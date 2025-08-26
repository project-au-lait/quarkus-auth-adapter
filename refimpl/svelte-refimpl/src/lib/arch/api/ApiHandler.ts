import { env } from '$env/dynamic/public';
import messageStore from '$lib/arch/global/MessageStore';
import { Api, type HttpResponse, type RequestParams } from './Api';
import accessTokenStore from '../auth/AccessTokenStore';
import { get } from 'svelte/store';

export default class ApiHandler {
	static getApi(
		fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>,
		params: RequestParams = {},
		accessToken?: string | null
	): Api<unknown> {
		const baseUrl = env.PUBLIC_BACKEND_URL || new URL(window.location.href).origin;

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

	static async handle<D>(
		fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>,
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
			await this.refreshAccessToken(fetch);
		} else {
			// TODO error handling
			messageStore.show(response.statusText);
			return undefined;
		}
	}

	static async refreshAccessToken(
		fetch: (input: RequestInfo | URL, init?: RequestInit | undefined) => Promise<Response>
	): Promise<void> {
		console.log('Refreshing access token...');

		const api = this.getApi(fetch, { credentials: 'include' });
		const response = await api.auth.refreshTokenList();

		if (response.ok) {
			const newAccessToken = response.data.accessToken;
			accessTokenStore.set(newAccessToken);
			console.log('Access token refreshed successfully:', newAccessToken);
		} else {
			accessTokenStore.set(null);
			console.error('Failed to refresh access token:', response.status, response.statusText);
		}
	}
}
