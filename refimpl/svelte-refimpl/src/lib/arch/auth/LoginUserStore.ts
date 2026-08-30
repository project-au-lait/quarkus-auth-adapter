import { get, writable } from 'svelte/store';
import apiHandler, { type AccessTokenHolder } from '../api/ApiHandler';
import type { LoginResponse, MeResponse } from '../api/Api';

class LoginUserModel {
  accessToken = '';
  firstName = '';
  lastName = '';
  roles: string[] = [];

  get isLoggedIn(): boolean {
    return this.accessToken !== '';
  }

  hasRole(role: string): boolean {
    return this.roles.includes(role);
  }
}

type Fetch = typeof fetch;

class LoginUserStore implements AccessTokenHolder {
  private readonly writable = writable(new LoginUserModel());
  private readonly update = this.writable.update;
  readonly subscribe = this.writable.subscribe;

  get token(): string {
    return get(this).accessToken;
  }

  set token(accessToken: string) {
    this.setAccessToken(accessToken);
  }

  clear(): void {
    this.logout();
  }

  async login(userName: string, password: string, fetch: Fetch): Promise<boolean> {
    const res = await apiHandler.handle<LoginResponse>(fetch, (api) =>
      api.auth.loginCreate(
        { userName, password },
        {
          credentials: 'include'
        }
      )
    );

    if (res) {
      this.setAccessToken(res.accessToken ?? '');
      await this.me(fetch);
      return true;
    }
    return false;
  }

  logout(): void {
    this.setAccessToken('');
  }

  private async me(fetch: Fetch): Promise<void> {
    const res = await apiHandler.handle<MeResponse>(fetch, (api) =>
      api.auth.meList({
        credentials: 'include'
      })
    );

    if (res) {
      this.update((user) => {
        user.firstName = res.firstName;
        user.lastName = res.lastName;
        user.roles = res.roles ?? [];
        return user;
      });
    }
  }

  setAccessToken(accessToken: string): void {
    this.update((user) => {
      user.accessToken = accessToken;

      if (accessToken) {
        this.me(fetch);
      } else {
        user.firstName = '';
        user.lastName = '';
        user.roles = [];
      }

      return user;
    });
  }
}

const loginUserStore = new LoginUserStore();
apiHandler.tokenHolder = loginUserStore;
export { loginUserStore };
export default loginUserStore;
