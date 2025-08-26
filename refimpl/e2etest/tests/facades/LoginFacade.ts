import { Browser, expect } from '@playwright/test';
import topPageFacade from './TopPageFacade';

class LoginFacade {
  constructor() {}

  async login(browser: Browser, storageState?: any) {
    const { context, topPage } = await topPageFacade.open(browser, storageState);

    const loginPage = await topPage.gotoLoginPage();

    const privatePage = storageState ? await topPage.gotoPrivatePage() : await loginPage.okLogin();

    return { context, privatePage };
  }

  async loginCheck(browser: Browser, userName: string, password: string) {
    const { topPage } = await topPageFacade.open(browser);

    const loginPage = await topPage.gotoLoginPage();

    const privatePage = await loginPage.okLogin(userName, password);
    await privatePage.expectLoggedIn();
  }
}

const loginFacade = new LoginFacade();
export default loginFacade;
