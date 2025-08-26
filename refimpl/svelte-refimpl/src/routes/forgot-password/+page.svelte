<script lang="ts">
  import ApiHandler from '$lib/arch/api/ApiHandler';
  import type { ForgotPasswordRequest } from '$lib/arch/api/Api';

  let email = 'provider-1@sqk.dev';

  async function requestReset() {
    const data: ForgotPasswordRequest = { email };

    const result = await ApiHandler.handle<void>(fetch, (api) =>
      api.auth.forgotPasswordCreate(data)
    );

    if (result !== undefined) {
      alert('Reset email sent');
    } else {
      alert('Failed to send email');
    }
  }
</script>

<h3>Forgot Your Password?</h3>

<label>
  Username
  <input id="email" bind:value={email} placeholder="email" />
</label>
<button id="send-reset-link" onclick={requestReset}>Submit</button>
<p>
  Enter your username or email address and we will send you instructions on how to create a new
  password.
</p>
