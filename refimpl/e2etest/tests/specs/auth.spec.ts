import loginFacade from '@facades/LoginFacade';
import { test } from '@playwright/test';

test('user details and roles', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  await privatePage.expectUserDetails('ProviderFirstName ProviderLastName', 'provider');
});
