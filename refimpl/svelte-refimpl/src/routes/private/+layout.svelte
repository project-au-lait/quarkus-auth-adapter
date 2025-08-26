<script lang="ts">
	import accessTokenStore from '$lib/arch/auth/AccessTokenStore';
	import LoginForm from '$lib/arch/auth/LoginForm.svelte';
</script>

{#if $accessTokenStore}
	<slot />
{:else}
	<LoginForm />
{/if}
