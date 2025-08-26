import loginFacade from '@facades/LoginFacade';
import passwordResetFacade from '@facades/PasswordResetFacade';
import { UserFactory } from '@factories/UserFactory';
import { test } from '@playwright/test';

test('password reset', async ({ browser }) => {
  const user = UserFactory.createUser1();
  const newPass = 'new-password';

  await passwordResetFacade.resetPassword(browser, user.email, newPass);
  await loginFacade.loginCheck(browser, user.email, newPass);

  await passwordResetFacade.resetPassword(browser, user.email, user.password);
  await loginFacade.loginCheck(browser, user.email, user.password);
});
