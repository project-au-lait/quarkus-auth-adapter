import BasePage from '@arch/BasePage';
import { DryRun } from '@arch/DryRun';
import { Page } from '@playwright/test';
import LoginPage from './LoginPage';

export default abstract class LayoutPage extends BasePage {
  private readonly ACCSESSTOKEN_SELECTOR = '#access-token';
  private readonly REFRESHTOKEN_SELECTOR = '#refresh-token';
  private readonly LOGIN_STATUS_SELECTOR = '#login-status';

  constructor(page: Page, dryRun: DryRun) {
    super({ page, dryRun });
  }

  async gotoLoginPage() {
    await this.click('#loginLink');
    return new LoginPage(this.page, this.dryRun);
  }

  async getAccessToken() {
    return await this.getInnerText(this.ACCSESSTOKEN_SELECTOR);
  }

  async getRefreshToken() {
    return await this.getInnerText(this.REFRESHTOKEN_SELECTOR);
  }

  async expectLoggedIn() {
    await this.expectInnerText(this.LOGIN_STATUS_SELECTOR, 'Logged in');
  }

  async expectLoggedOut() {
    await this.expectInnerText(this.LOGIN_STATUS_SELECTOR, 'Not logged in');
    await this.expectNotVisible(this.ACCSESSTOKEN_SELECTOR);
    await this.expectNotVisible(this.REFRESHTOKEN_SELECTOR);
  }

  async expectAccessTokenNot(accessToken: string) {
    await this.expectInnerTextNot(this.ACCSESSTOKEN_SELECTOR, accessToken);
  }
}
