import { messageStore } from '$lib/arch/global/MessageStore';
import apiClient, { type ApiClient, type RequestHandler } from './ApiClient';

type Fetch = typeof fetch;
type Handler = RequestHandler;

export interface AccessTokenHolder {
  set token(accessToken: string);
  get token(): string;
  clear(): void;
}

class DefaultAccessTokenHolder implements AccessTokenHolder {
  token = '';
  clear(): void {
    this.token = '';
  }
}

export class ApiHandler {
  tokenHolder: AccessTokenHolder = new DefaultAccessTokenHolder();
  private refreshInFlight: Promise<boolean> | null = null;

  constructor(private readonly client: ApiClient = apiClient) {}

  async handle<D>(fetch: Fetch, handler: Handler): Promise<D | undefined> {
    const { data, response } = await this.client.execute<D>(fetch, this.tokenHolder.token, handler);

    if (response.ok) return data as D;

    if (response.status === 401) {
      const refreshed = await this.refreshAccessToken(fetch);
      if (refreshed) return this.retry<D>(fetch, handler);
    }

    messageStore.show(response.statusText);
  }

  private async retry<D>(fetch: Fetch, handler: Handler): Promise<D | undefined> {
    const { data, response } = await this.client.execute<D>(fetch, this.tokenHolder.token, handler);
    if (!response.ok) {
      messageStore.show(response.statusText);
      return;
    }
    return data as D;
  }

  async handleWithDefault<D>(fetch: Fetch, handler: Handler): Promise<D> {
    const result = await this.handle<D>(fetch, handler);
    return result ?? ({} as D);
  }

  async refreshAccessToken(fetch: Fetch): Promise<boolean> {
    this.refreshInFlight ??= (async () => {
      const accessToken = await this.client.refreshAccessToken(fetch);

      if (accessToken) {
        this.tokenHolder.token = accessToken;
        return true;
      }

      this.tokenHolder.clear();
      return false;
    })().finally(() => {
      this.refreshInFlight = null;
    });

    return this.refreshInFlight;
  }
}

const apiHandler = new ApiHandler();
export default apiHandler;
