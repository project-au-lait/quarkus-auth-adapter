import BasePage from '@arch/BasePage';
import { DryRun } from '@arch/DryRun';
import { Page } from '@playwright/test';

export default class ResetPasswordPage extends BasePage {
  constructor(page: Page, dryRun: DryRun) {
    super({ page, dryRun });
  }

  get pageNameKey() {
    return 'reset-password';
  }

  async open(resetPasswordUrl: string) {
    await super.open(resetPasswordUrl);
  }

  async resetPassword(newPassword: string) {
    await this.inputText('#new-password', newPassword);
    await this.click('#reset-password');
    await this.getGlobalMessage();
  }
}
