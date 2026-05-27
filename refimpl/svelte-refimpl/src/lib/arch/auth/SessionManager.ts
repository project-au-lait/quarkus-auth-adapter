import ApiHandler from '$lib/arch/api/ApiHandler';
import type { LoginRequest } from '$lib/arch/api/Api';
import accessTokenStore from './AccessTokenStore';
import { env } from '$env/dynamic/public';

type FetchFn = typeof globalThis.fetch;

export default class SessionManager {
  static async login(fetch: FetchFn, loginRequest: LoginRequest): Promise<boolean> {
    const tokenEndpoint = env.PUBLIC_TOKEN_ENDPOINT;
    const api = ApiHandler.getApi(fetch, { credentials: 'include' }, null, {
      htu: tokenEndpoint,
      htm: 'POST'
    });
    const response = await api.auth.loginCreate(loginRequest);

    if (response.ok && response.data) {
      accessTokenStore.set(response.data.accessToken ?? null);
      return true;
    }
    return false;
  }

  static async refreshAccessToken(fetch: FetchFn): Promise<void> {
    const tokenEndpoint = env.PUBLIC_TOKEN_ENDPOINT;
    const api = ApiHandler.getApi(fetch, { credentials: 'include' }, undefined, {
      htu: tokenEndpoint,
      htm: 'POST'
    });
    const response = await api.auth.refreshTokenList();

    if (response.ok) {
      accessTokenStore.set(response.data.accessToken ?? null);
    } else {
      accessTokenStore.set(null);
    }
  }
}
