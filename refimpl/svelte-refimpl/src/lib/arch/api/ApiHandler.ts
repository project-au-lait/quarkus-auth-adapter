import { env } from '$env/dynamic/public';
import messageStore from '$lib/arch/global/MessageStore';
import { Api, type HttpResponse, type RequestParams } from './Api';
import accessTokenStore from '../auth/AccessTokenStore';
import { buildDPoPProof, updateNonce, type DPoPOverrides } from '../auth/DPoPProof';
import { get } from 'svelte/store';

function isDPoPMode(): boolean {
  return env.PUBLIC_AUTH_MODE === 'dpop';
}

export default class ApiHandler {
  static getApi(
    fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>,
    params: RequestParams = {},
    accessToken?: string | null,
    dpopOverrides?: DPoPOverrides
  ): Api<unknown> {
    const baseUrl = env.PUBLIC_BACKEND_URL || new URL(window.location.href).origin;

    const baseApiParams: RequestParams = accessToken
      ? {
          secure: true,
          headers: {
            Authorization: isDPoPMode() ? `DPoP ${accessToken}` : `Bearer ${accessToken}`
          },
          ...params
        }
      : {
          ...params
        };

    const customFetch = isDPoPMode()
      ? this.wrapFetchWithDPoP(fetch, accessToken, dpopOverrides)
      : fetch;

    return new Api({
      baseUrl,
      baseApiParams,
      customFetch
    });
  }

  private static wrapFetchWithDPoP(
    baseFetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>,
    accessToken?: string | null,
    dpopOverrides?: DPoPOverrides
  ): (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response> {
    return async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const dpopProof = await buildDPoPProof(input, init, accessToken ?? undefined, dpopOverrides);

      const headers = new Headers(init?.headers);
      headers.set('DPoP', dpopProof);
      if (accessToken) {
        headers.set('Authorization', `DPoP ${accessToken}`);
      }
      init = { ...init, headers };

      const response = await baseFetch(input, init);

      const nonce = response.headers.get('DPoP-Nonce');
      if (nonce) {
        updateNonce(nonce);
      }

      // AS nonce: 400 + DPoP-Nonce header
      // RS nonce: 401 + WWW-Authenticate: DPoP error="use_dpop_nonce" + DPoP-Nonce header
      if (nonce && (response.status === 401 || response.status === 400)) {
        const wwwAuth = response.headers.get('WWW-Authenticate') ?? '';
        const isNonceError =
          (response.status === 401 && wwwAuth.includes('use_dpop_nonce')) ||
          response.status === 400;
        if (isNonceError) {
          const retryProof = await buildDPoPProof(
            input,
            init,
            accessToken ?? undefined,
            dpopOverrides
          );
          const retryHeaders = new Headers(init?.headers);
          retryHeaders.set('DPoP', retryProof);
          if (accessToken) {
            retryHeaders.set('Authorization', `DPoP ${accessToken}`);
          }
          return baseFetch(input, { ...init, headers: retryHeaders });
        }
      }

      return response;
    };
  }

  static async handle<D>(
    fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>,
    handler: (api: Api<unknown>) => Promise<HttpResponse<D, unknown>>,
    params: RequestParams = {},
    options?: { dpopOverrides?: DPoPOverrides; retryTokenRefresh?: boolean }
  ): Promise<D | undefined> {
    const { dpopOverrides, retryTokenRefresh = true } = options ?? {};
    const accessToken = get(accessTokenStore);
    const api = this.getApi(fetch, params, accessToken, dpopOverrides);

    const response = await handler(api);

    if (response.ok) {
      return response.data || (response.text() as D);
    } else if (response.status === 401 && retryTokenRefresh) {
      await this.refreshAccessToken(fetch);
      const newAccessToken = get(accessTokenStore);
      const retryApi = this.getApi(fetch, params, newAccessToken, dpopOverrides);
      const retryResponse = await handler(retryApi);
      if (retryResponse.ok) {
        return retryResponse.data || (retryResponse.text() as D);
      }
      messageStore.show(retryResponse.statusText);
    } else {
      messageStore.show(response.statusText);
      return undefined;
    }
  }

  static async refreshAccessToken(
    fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>
  ): Promise<void> {
    console.log('Refreshing access token...');

    let api: Api<unknown>;
    if (isDPoPMode()) {
      const tokenEndpoint = env.PUBLIC_TOKEN_ENDPOINT;
      const dpopOverrides: DPoPOverrides = { htu: tokenEndpoint, htm: 'POST' };
      api = this.getApi(fetch, { credentials: 'include' }, null, dpopOverrides);
    } else {
      api = this.getApi(fetch, { credentials: 'include' });
    }

    const response = await api.auth.refreshTokenList();

    if (response.ok) {
      const newAccessToken = response.data.accessToken ?? null;
      accessTokenStore.set(newAccessToken);
      console.log('Access token refreshed successfully:', newAccessToken);
    } else {
      accessTokenStore.set(null);
      console.error('Failed to refresh access token:', response.status, response.statusText);
    }
  }
}
