import BasePage from '@arch/BasePage';
import { DryRun } from '@arch/DryRun';
import { expect, Page } from '@playwright/test';
import LayoutPage from './LayoutPage';
export default class PrivatePage extends LayoutPage {
  constructor(page: Page, dryRun: DryRun) {
    super(page, dryRun);
  }

  get pageNameKey() {
    return 'private';
  }

  async clickApiCallButton() {
    await this.click('#apiCall');
  }
}
