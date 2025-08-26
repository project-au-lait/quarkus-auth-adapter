<script lang="ts">
  import { page } from '$app/stores';
  import ApiHandler from '$lib/arch/api/ApiHandler';
  import type { ResetPasswordRequest } from '$lib/arch/api/Api';
  import messageStore from '$lib/arch/global/MessageStore';

  let newPassword = '';
  $: code = new URLSearchParams($page.url.search).get('code') || '';

  async function submit() {
    const data: ResetPasswordRequest = { code, newPassword };

    const result = await ApiHandler.handle<void>(fetch, (api) =>
      api.auth.resetPasswordCreate(data)
    );

    if (result !== undefined) {
      messageStore.show('Password has been reset');
    }
  }
</script>

<h3>Reset Password</h3>

<section>
  <label>
    New Password:
    <input id="new-password" type="password" bind:value={newPassword} placeholder="new password" />
  </label>
  <button id="reset-password" onclick={submit}>Reset password</button>
</section>
