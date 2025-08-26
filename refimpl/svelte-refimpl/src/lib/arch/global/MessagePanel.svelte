<script lang="ts">
	import messageStore from './MessageStore';
</script>

{#if $messageStore.display}
	<article class="message">
		<span id="globalMessage"> {$messageStore.text}</span>
		<button class="close" aria-label="close" onclick={() => messageStore.hide()}></button>
	</article>
{/if}

<style scoped>
	article.message {
		display: flex;
		justify-content: space-between;
	}

	button.close {
		background-image: var(--pico-icon-close);
		background-color: transparent;
		border: none;
		background-size: auto 1rem;
	}
</style>
