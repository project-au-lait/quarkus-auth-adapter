<script lang="ts">
  import { loginUserStore } from '$lib/arch/auth/LoginUserStore';
  import LoginForm from '$lib/arch/auth/LoginForm.svelte';
</script>

{#if $loginUserStore.isLoggedIn}
  <slot />
{:else}
  <LoginForm />
{/if}
