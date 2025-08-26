import 'dotenv/config';
import BasePage from '@arch/BasePage';
import { Page } from '@playwright/test';
import { DryRun } from '@arch/DryRun';
import MailpitViewPage from '@pages/MailpitViewPage';

export default class MailpitTopPage extends BasePage {
  constructor(page: Page, dryRun: DryRun) {
    super({ page, dryRun });
  }

  get pageNameKey() {
    return 'mailpit-top';
  }

  async open() {
    const smtpUrl = process.env.SMTP_WEB_URL;
    if (!smtpUrl) {
      throw new Error('SMTP_WEB_URL environment variable is not defined');
    }
    await super.open(smtpUrl);
  }

  async gotoViewPage() {
    await this.click('a.message:nth-of-type(1)');
    return new MailpitViewPage(this.page, this.dryRun);
  }
}
