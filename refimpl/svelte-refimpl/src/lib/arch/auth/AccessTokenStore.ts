import { writable } from 'svelte/store';

const accessTokenStore = writable<string | null>(null);

export default accessTokenStore;
