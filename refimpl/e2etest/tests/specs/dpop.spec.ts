import loginFacade from '@facades/LoginFacade';
import TimeoutFactory from '@factories/TimeoutFactory';
import { expect, test } from '@playwright/test';

test('dpop login', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  await privatePage.expectLoggedIn();

  const accessToken = await privatePage.getAccessToken();
  expect(accessToken).toBeTruthy();

  // Verify DPoP header is sent on API call
  const page = privatePage.page;
  const requestPromise = page.waitForRequest((req) => req.url().includes('/restricted'));
  await privatePage.clickApiCallButton();
  const request = await requestPromise;

  expect(request.headers()['dpop']).toBeTruthy();
  expect(request.headers()['authorization']).toMatch(/^DPoP /);
});

test('dpop token refresh', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  const tokenBefore = await privatePage.getAccessToken();
  await privatePage.waitFor(TimeoutFactory.accessTokenTimeout());
  await privatePage.clickApiCallButton();
  await privatePage.expectAccessTokenNot(tokenBefore);

  // Verify the refreshed token still uses DPoP
  const page = privatePage.page;
  const requestPromise = page.waitForRequest((req) => req.url().includes('/restricted'));
  await privatePage.clickApiCallButton();
  const request = await requestPromise;

  expect(request.headers()['dpop']).toBeTruthy();
  expect(request.headers()['authorization']).toMatch(/^DPoP /);
});

test('dpop restricted access', async ({ browser }) => {
  const { privatePage } = await loginFacade.login(browser);

  const page = privatePage.page;

  // Wait for the final 200 response regardless of internal nonce retry (401 → 200)
  const responsePromise = page.waitForResponse(
    (res) => res.url().includes('/restricted') && res.status() === 200
  );
  await privatePage.clickApiCallButton();
  await responsePromise;

  const accessToken = await privatePage.getAccessToken();
  expect(accessToken).toBeTruthy();
});
