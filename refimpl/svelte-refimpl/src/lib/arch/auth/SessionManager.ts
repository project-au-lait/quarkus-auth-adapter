import ApiHandler from '$lib/arch/api/ApiHandler';
import type { LoginRequest } from '$lib/arch/api/Api';
import accessTokenStore from './AccessTokenStore';

type FetchFn = typeof globalThis.fetch;

export default class SessionManager {
  static async login(fetch: FetchFn, loginRequest: LoginRequest): Promise<boolean> {
    const api = ApiHandler.getApi(fetch, { credentials: 'include' }, null);
    const response = await api.auth.loginCreate(loginRequest);

    if (response.ok && response.data) {
      accessTokenStore.set(response.data.accessToken ?? null);
      return true;
    }
    return false;
  }

  static async refreshAccessToken(fetch: FetchFn): Promise<void> {
    const api = ApiHandler.getApi(fetch, { credentials: 'include' });
    const response = await api.auth.refreshTokenList();

    if (response.ok) {
      accessTokenStore.set(response.data.accessToken ?? null);
    } else {
      accessTokenStore.set(null);
    }
  }
}
