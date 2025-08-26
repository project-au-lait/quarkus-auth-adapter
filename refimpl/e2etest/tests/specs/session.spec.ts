import loginFacade from '@facades/LoginFacade';
import TimeoutFactory from '@factories/TimeoutFactory';
import { test } from '@playwright/test';

test('session keep', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  await privatePage.waitFor(TimeoutFactory.accessTokenTimeout());
  await privatePage.clickApiCallButton();
  await privatePage.expectLoggedIn();
});

test('session timeout', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  await privatePage.waitFor(TimeoutFactory.refreshTokenTimeout());
  await privatePage.clickApiCallButton();
  await privatePage.expectLoggedOut();
});
