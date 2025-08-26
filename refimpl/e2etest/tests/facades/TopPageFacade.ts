import { DryRun } from '@arch/DryRun';
import TopPage from '@pages/TopPage';
import { Browser } from '@playwright/test';

class TopPageFacade {
  async open(browser: Browser, storageState?: any) {
    const context = storageState
      ? await browser.newContext({ storageState })
      : await browser.newContext();

    const page = await context.newPage();
    const dryRun = DryRun.build();

    const topPage = new TopPage(page, dryRun);
    await topPage.open();

    return { context, topPage };
  }
}

const topPageFacade = new TopPageFacade();
export default topPageFacade;
