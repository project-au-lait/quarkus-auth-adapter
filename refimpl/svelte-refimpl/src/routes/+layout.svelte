<script lang="ts">
  import '@picocss/pico';
  import { onMount } from 'svelte';
  import MessagePanel from '$lib/arch/global/MessagePanel.svelte';
  import { loginUserStore } from '$lib/arch/auth/LoginUserStore';
  import apiHandler from '$lib/arch/api/ApiHandler';

  onMount(async () => {
    await apiHandler.refreshAccessToken(fetch);
  });
</script>

<nav class="container">
  <ul>
    <li><strong>Auth</strong></li>
  </ul>
  <ul>
    <li><a href="/">Top</a></li>
    {#if $loginUserStore.isLoggedIn}
      <li>
        <button on:click={() => loginUserStore.logout()}>Logout</button>
      </li>
      <li id="login-status">Logged in</li>
    {:else}
      <li>
        <a id="loginLink" href="/private">Login</a>
      </li>
      <li id="login-status">Not logged in</li>
    {/if}
  </ul>
</nav>

<main class="container">
  <MessagePanel />

  <slot />
</main>

<footer class="container">
  {#if $loginUserStore.isLoggedIn}
    <p class="user">
      User: <span id="user-full-name">{$loginUserStore.firstName} {$loginUserStore.lastName}</span>
      Roles: <span id="user-roles">{$loginUserStore.roles.join(', ')}</span>
      Has Provider Role: <span id="has-provider-role">{$loginUserStore.hasRole('provider')}</span>
      Has Admin Role: <span id="has-admin-role">{$loginUserStore.hasRole('admin')}</span>
    </p>
    <p class="token">
      AccessToken: <span id="access-token">{$loginUserStore.accessToken}</span>
    </p>
  {/if}
</footer>

<style>
  :global(:root) {
    --pico-font-size: small;
  }

  main {
    min-height: 50vh;
  }

  .token {
    overflow: scroll;
    white-space: nowrap;
  }
</style>
