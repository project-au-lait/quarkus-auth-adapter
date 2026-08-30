import { get, writable } from 'svelte/store';
import apiHandler, { type AccessTokenHolder } from '../api/ApiHandler';

class AccessTokenStore implements AccessTokenHolder {
  private readonly writable = writable<string | null>(null);
  private readonly update = this.writable.update;
  readonly subscribe = this.writable.subscribe;
  readonly set = this.writable.set;

  get token(): string {
    return get(this.writable) ?? '';
  }

  set token(accessToken: string) {
    this.writable.set(accessToken || null);
  }

  clear(): void {
    this.writable.set(null);
  }
}

const accessTokenStore = new AccessTokenStore();
apiHandler.tokenHolder = accessTokenStore;
export default accessTokenStore;
