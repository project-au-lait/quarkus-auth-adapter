import BasePage from '@arch/BasePage';
import { DryRun } from '@arch/DryRun';
import ForgotPasswordPage from '@pages/ForgotPasswordPage';
import PrivatePage from '@pages/PrivatePage';
import { Page } from '@playwright/test';
import LayoutPage from './LayoutPage';

export default class TopPage extends LayoutPage {
  constructor(page: Page, dryRun: DryRun) {
    super(page, dryRun);
  }

  get pageNameKey() {
    return 'top';
  }

  async open() {
    await super.open('/');
  }

  async gotoPrivatePage() {
    await this.click('#private');
    return new PrivatePage(this.page, this.dryRun);
  }

  async gotoForgotPasswordPage() {
    await this.click('#forgot-password');
    return new ForgotPasswordPage(this.page, this.dryRun);
  }
}
