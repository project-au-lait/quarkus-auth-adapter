import loginFacade from '@facades/LoginFacade';
import topPageFacade from '@facades/TopPageFacade';
import TimeoutFactory from '@factories/TimeoutFactory';
import { expect, test } from '@playwright/test';
import { log } from 'console';

test('token refresh', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  const tokenBefore = await privatePage.getAccessToken();
  await privatePage.waitFor(TimeoutFactory.accessTokenTimeout());
  await privatePage.clickApiCallButton();
  await privatePage.expectAccessTokenNot(tokenBefore);
});

test('token restore', async ({ browser }, testInfo) => {
  test.skip(testInfo.project.name !== 'chromium', 'This test only runs on Chromium');

  const { context, privatePage } = await loginFacade.login(browser);

  await privatePage.clickApiCallButton();
  const storageState = await context.storageState();
  const refreshToken = storageState.cookies.find((cookie) => cookie.name === 'refreshToken');
  expect(refreshToken).toBeDefined();

  await context.close();

  const { topPage } = await topPageFacade.open(browser, storageState);

  await topPage.expectLoggedIn();
});
