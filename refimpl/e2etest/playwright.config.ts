import { defineConfig, devices } from '@playwright/test';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 1,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
    actionTimeout: 3000,
    navigationTimeout: 3000
  },

  webServer: [
    {
      command: 'pnpm -C ../svelte-refimpl preview --port 5173',
      url: 'http://localhost:5173',
      reuseExistingServer: true,
      timeout: 30000
    },
    {
      command: 'pnpm -C ../svelte-refimpl preview --port 5174',
      url: 'http://localhost:5174',
      reuseExistingServer: true,
      timeout: 30000,
      env: {
        PUBLIC_AUTH_MODE: 'dpop',
        PUBLIC_BACKEND_URL: 'http://localhost:8080',
        PUBLIC_TOKEN_ENDPOINT:
          'http://localhost:8085/realms/qaa-realm/protocol/openid-connect/token'
      }
    }
  ],

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'http://localhost:5173'
      },
      testIgnore: /dpop\.spec/
    },
    {
      name: 'chromium-dpop',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'http://localhost:5174'
      },
      testMatch: /dpop\.spec/
    }

    /* Scope out temporarily */
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] }
    // },

    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] }
    // }

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ]
});
