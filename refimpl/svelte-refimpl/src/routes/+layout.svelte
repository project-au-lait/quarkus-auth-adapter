<script lang="ts">
	import '@picocss/pico';
	import { onMount } from 'svelte';
	import MessagePanel from '$lib/arch/global/MessagePanel.svelte';
	import accessTokenStore from '$lib/arch/auth/AccessTokenStore';
	import ApiHandler from '$lib/arch/api/ApiHandler';

	onMount(async () => {
		await ApiHandler.refreshAccessToken(fetch);
	});
</script>

<nav class="container">
	<ul>
		<li><strong>Auth</strong></li>
	</ul>
	<ul>
		<li><a href="/">Top</a></li>
		{#if $accessTokenStore}
			<li>
				<button on:click={() => accessTokenStore.set(null)}>Logout</button>
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
	{#if $accessTokenStore}
		<p class="token">
			AccessToken: <span id="access-token">{$accessTokenStore}</span>
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
