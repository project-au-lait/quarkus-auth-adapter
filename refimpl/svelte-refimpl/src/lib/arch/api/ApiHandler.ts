import { env } from '$env/dynamic/public';
import messageStore from '$lib/arch/global/MessageStore';
import { Api, type HttpResponse, type RequestParams } from './Api';
import accessTokenStore from '../auth/AccessTokenStore';
import { get } from 'svelte/store';
import { buildDPoPProof, updateNonce } from '../auth/DPoPProof';
import SessionManager from '../auth/SessionManager';

type FetchFn = typeof globalThis.fetch;

export default class ApiHandler {
  static async callAuthenticated<D>(
    fetch: FetchFn,
    handler: (api: Api<unknown>) => Promise<HttpResponse<D, unknown>>,
    params: RequestParams = {}
  ): Promise<D | undefined> {
    const accessToken = get(accessTokenStore);
    const api = this.getApi(fetch, params, accessToken);

    // TODO show loading

    const response = await handler(api);

    if (response.ok) {
      return response.data || (response.text() as D);
    } else if (response.status === 401) {
      await SessionManager.refreshAccessToken(fetch);
      const newAccessToken = get(accessTokenStore);
      if (newAccessToken) {
        const retryApi = this.getApi(fetch, params, newAccessToken);
        const retryResponse = await handler(retryApi);
        if (retryResponse.ok) {
          return retryResponse.data || (retryResponse.text() as D);
        }
      }
    } else {
      // TODO error handling
      messageStore.show(response.statusText);
      return undefined;
    }
  }

  static getApi(
    fetch: FetchFn,
    params: RequestParams = {},
    accessToken?: string | null
  ): Api<unknown> {
    const baseUrl = this.getBaseUrl();
    return new Api({
      baseUrl,
      baseApiParams: params,
      customFetch: this.createDPoPFetch(fetch, accessToken)
    });
  }

  private static createDPoPFetch(baseFetch: FetchFn, accessToken?: string | null): FetchFn {
    return async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const dpopProof = await buildDPoPProof(input, init, accessToken ?? undefined);

      const headers = new Headers(init?.headers);
      headers.set('DPoP', dpopProof);
      if (accessToken) {
        headers.set('Authorization', `DPoP ${accessToken}`);
      }

      const response = await baseFetch(input, { ...init, headers });

      const nonce = response.headers.get('DPoP-Nonce');
      if (nonce) {
        updateNonce(nonce);
      }

      if (response.status === 401 && nonce) {
        const body = await response
          .clone()
          .json()
          .catch(() => null);
        if (body?.error === 'use_dpop_nonce') {
          const retryProof = await buildDPoPProof(input, init, accessToken ?? undefined);
          headers.set('DPoP', retryProof);
          return baseFetch(input, { ...init, headers });
        }
      }

      return response;
    };
  }

  private static getBaseUrl(): string {
    return env.PUBLIC_BACKEND_URL || new URL(window.location.href).origin;
  }
}
