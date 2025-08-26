import { expect, type Page } from '@playwright/test';
import { Action, DryRun } from '@arch/DryRun';

export default abstract class BasePage {
  private readonly GLOBAL_MESSAGE_SELECTOR = '#globalMessage';

  page: Page;
  dryRun: DryRun;

  constructor(page: BasePage | { page: Page; dryRun: DryRun }) {
    this.page = page.page;
    this.dryRun = page.dryRun;
  }

  abstract get pageNameKey(): string;

  private async run(
    action: Action,
    itemNameKey: string,
    runAction: () => Promise<any>,
    additionalValue?: string
  ) {
    const log = this.dryRun.log(this.pageNameKey, itemNameKey, action, additionalValue);

    if (this.dryRun.isOn) {
      return;
    }

    await this.consoleLog(log);

    await runAction();
  }

  private async consoleLog(log: string) {
    await this.page.evaluate(`console.log(\`${log}\`);`);
  }

  protected async inputText(selector: string, value: any) {
    await this.run(
      Action.INPUT,
      selector,
      () => this.page.locator(selector).fill(value.toString()),
      value
    );
  }

  protected async selectOption(selector: string, value: any) {
    await this.run(
      Action.INPUT,
      selector,
      () => this.page.locator(selector).selectOption(value),
      value
    );
  }

  protected async check(selector: string, value: boolean) {
    await this.run(Action.INPUT, selector, () => this.page.locator(selector).setChecked(value));
  }

  protected async open(path: string) {
    await this.run(Action.GOTO, path, () => this.page.goto(path));
  }

  protected async click(selector: string) {
    await this.run(Action.CLICK, selector, () => this.page.locator(selector).click());
  }

  protected async clickByRole(role: string, name: string) {
    const selectorInfo = `${role}[name="${name}"]`;
    await this.run(Action.CLICK, selectorInfo, () =>
      this.page.getByRole(role as any, { name }).click()
    );
  }

  protected async expectGlobalMessage(message: string) {
    await this.run(Action.EXPECT_VISIBLE, message, () =>
      expect(this.page.locator(this.GLOBAL_MESSAGE_SELECTOR)).toHaveText(message)
    );
  }

  async expectNotVisible(selector: string) {
    await this.run(Action.EXPECT_NOT_VISIBLE, selector, () =>
      expect(this.page.locator(selector)).not.toBeVisible()
    );
  }

  protected async expectInnerText(selector: string, expectedText: string) {
    await this.run(
      Action.EXPECT_TEXT,
      selector,
      () => expect(this.page.locator(selector)).toHaveText(expectedText),
      expectedText
    );
  }

  protected async expectInnerTextNot(selector: string, expectedText: string) {
    await this.run(
      Action.EXPECT_TEXT,
      selector,
      () => expect(this.page.locator(selector)).not.toHaveText(expectedText),
      expectedText
    );
  }

  protected async getInnerText(selector: string): Promise<string> {
    let value = '';
    await this.run(Action.GET_TEXT, selector, async () => {
      value = await this.page.locator(selector).innerText();
    });
    return value;
  }

  protected async getGlobalMessage() {
    return await this.getInnerText(this.GLOBAL_MESSAGE_SELECTOR);
  }

  protected getFrameLocator(selector: string) {
    return this.page.frameLocator(selector);
  }

  public async waitFor(milliseconds: number) {
    await this.run(Action.WAIT, `${milliseconds}ms`, () => this.page.waitForTimeout(milliseconds));
  }
}
