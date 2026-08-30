<script lang="ts">
  import type { LoginRequest } from '$lib/arch/api/Api';
  import messageStore from '../global/MessageStore';
  import { loginUserStore } from './LoginUserStore';

  const loginRequest: LoginRequest = {
    userName: 'provider-1',
    password: 'provider-1'
  };

  async function login() {
    const success = await loginUserStore.login(
      loginRequest.userName ?? '',
      loginRequest.password ?? '',
      fetch
    );

    if (success) {
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
