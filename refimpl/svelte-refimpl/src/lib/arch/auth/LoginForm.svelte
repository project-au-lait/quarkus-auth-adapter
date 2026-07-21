<script lang="ts">
  import { env } from '$env/dynamic/public';
  import ApiHandler from '$lib/arch/api/ApiHandler';
  import type { LoginRequest, LoginResponse } from '$lib/arch/api/Api';
  import messageStore from '../global/MessageStore';
  import accessTokenStore from './AccessTokenStore';
  import type { DPoPOverrides } from './DPoPProof';

  const loginRequest: LoginRequest = {
    userName: 'provider-1',
    password: 'provider-1'
  };

  async function login() {
    let res: LoginResponse | undefined;

    if (env.PUBLIC_AUTH_MODE === 'dpop') {
      const tokenEndpoint = env.PUBLIC_TOKEN_ENDPOINT;
      const dpopOverrides: DPoPOverrides = { htu: tokenEndpoint, htm: 'POST' };
      res = await ApiHandler.handle<LoginResponse>(
        fetch,
        (api) => api.auth.loginCreate(loginRequest),
        { credentials: 'include' },
        { dpopOverrides, retryTokenRefresh: false }
      );
    } else {
      res = await ApiHandler.handle<LoginResponse>(
        fetch,
        (api) => api.auth.loginCreate(loginRequest),
        { credentials: 'include' },
        { retryTokenRefresh: false }
      );
    }

    if (res) {
      accessTokenStore.set(res.accessToken ?? null);
      messageStore.show('Login succeeded.');
    } else {
      messageStore.show('Login failed.');
    }
  }
</script>

<h3>Login</h3>

<section>
  <label
    >Username:
    <input id="userName" bind:value={loginRequest.userName} placeholder="Username" />
  </label>
  <label>
    Password:
    <input
      id="password"
      bind:value={loginRequest.password}
      type="password"
      placeholder="Password"
    />
  </label>
  <button id="login" on:click={login}>Login</button>
</section>
