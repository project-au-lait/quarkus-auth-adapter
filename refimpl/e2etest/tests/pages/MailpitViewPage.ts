import BasePage from '@arch/BasePage';

export default class MailpitViewPage extends BasePage {
  constructor(page: Page, dryRun: DryRun) {
    super({ page, dryRun });
  }

  get pageNameKey() {
    return 'mailpit-view';
  }

  async getResetPageUrl(): Promise<string> {
    const iframe = this.getFrameLocator('#preview-html');
    const linkLocator = iframe.locator('a:has-text("Link to account update")');
    const resetUrl = await linkLocator.getAttribute('href');

    if (!resetUrl) {
      throw new Error('Reset URL not found in the email');
    }

    return resetUrl;
  }

  async deleteEmailMessage() {
    await this.clickByRole('button', 'Delete');
  }
}
