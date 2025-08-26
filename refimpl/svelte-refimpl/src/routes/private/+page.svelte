<script lang="ts">
  import ApiHandler from '$lib/arch/api/ApiHandler';

  async function callApi() {
    await ApiHandler.handle<string>(fetch, (api) => api.restricted.restrictedList());
  }
</script>

<h3>Private Page</h3>

<section>
  <p>This page is restricted to authenticated users. You can access it after logging in.</p>
  <p>
    After no API call for a certain period, you will be logged out automatically(= session timeout).
    <br />
    You can extend the session by clicking the button below to call an API.
  </p>
  <button id="apiCall" on:click={callApi}>Call API</button>
</section>
