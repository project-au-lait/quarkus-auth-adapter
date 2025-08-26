import BasePage from '@arch/BasePage';
import { DryRun } from '@arch/DryRun';
import { Page } from '@playwright/test';
export default class ForgotPasswordPage extends BasePage {
  constructor(page: Page, dryRun: DryRun) {
    super({ page, dryRun });
  }

  get pageNameKey() {
    return 'forgot-password';
  }

  async submit(email: string) {
    await this.inputText('#email', email);
    await this.click('#send-reset-link');
  }
}
