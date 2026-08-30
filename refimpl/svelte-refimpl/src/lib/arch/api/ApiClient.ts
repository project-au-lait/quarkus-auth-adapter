import { env } from '$env/dynamic/public';
import { Api, type HttpResponse, type RequestParams } from './Api';

type Fetch = typeof fetch;

export interface ApiClient {
  execute<D>(fetch: Fetch, accessToken: string, handler: RequestHandler): Promise<ApiResponse<D>>;
  refreshAccessToken(fetch: Fetch): Promise<string | undefined>;
}

export interface ApiResponse<D> {
  data: D | undefined;
  response: Response;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type RequestHandler = (api: Api<unknown>) => Promise<HttpResponse<any, any>>;

class SwaggerApiClient implements ApiClient {
  async execute<D>(
    fetch: Fetch,
    accessToken: string,
    handler: RequestHandler
  ): Promise<ApiResponse<D>> {
    const api = this.create(fetch, accessToken);
    const response = await handler(api);
    const data = response.ok
      ? (response.data ?? ((await response.text()) as unknown as D))
      : undefined;
    return { data, response };
  }

  async refreshAccessToken(fetch: Fetch): Promise<string | undefined> {
    const api = this.create(fetch, undefined, { credentials: 'include' });
    const response = await api.auth.refreshTokenList();
    return response.ok ? response.data?.accessToken : undefined;
  }

  private create(fetch: Fetch, accessToken?: string, params: RequestParams = {}): Api<unknown> {
    const baseUrl = env.PUBLIC_BACKEND_URL || new URL(globalThis.location.href).origin;
    const baseApiParams: RequestParams = accessToken
      ? {
          secure: true,
          headers: {
            Authorization: `Bearer ${accessToken}`
          },
          ...params
        }
      : {
          ...params
        };

    return new Api({
      baseUrl,
      baseApiParams,
      customFetch: fetch
    });
  }
}

const apiClient = new SwaggerApiClient();
export default apiClient;
