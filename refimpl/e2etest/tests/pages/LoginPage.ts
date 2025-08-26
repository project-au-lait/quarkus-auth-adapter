import BasePage from '@arch/BasePage';
import PrivatePage from './PrivatePage';

export default class LoginPage extends BasePage {
  constructor(page: any, dryRun: any) {
    super({ page, dryRun });
  }

  get pageNameKey() {
    return 'login';
  }

  async okLogin(userName?: string, password?: string) {
    if (userName) {
      await this.inputText('#userName', userName);
    }
    if (password) {
      await this.inputText('#password', password);
    }
    await this.click('#login');

    return new PrivatePage(this.page, this.dryRun);
  }
}
