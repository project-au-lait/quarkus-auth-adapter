<script lang="ts">
  import SessionManager from './SessionManager';
  import type { LoginRequest } from '$lib/arch/api/Api';
  import messageStore from '../global/MessageStore';

  const loginRequest: LoginRequest = {
    userName: 'provider-1',
    password: 'provider-1'
  };

  async function login() {
    const success = await SessionManager.login(fetch, loginRequest);
    messageStore.show(success ? 'Login succeeded.' : 'Login failed.');
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
