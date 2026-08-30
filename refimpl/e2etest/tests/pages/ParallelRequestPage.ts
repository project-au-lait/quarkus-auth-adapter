import { DryRun } from '@arch/DryRun';
import LayoutPage from '@pages/LayoutPage';
import { Page } from '@playwright/test';

export default class ParallelRequestPage extends LayoutPage {
  private readonly PARALLEL_CALLS_SELECTOR = '#parallel-calls';
  private readonly RUNS_SELECTOR = '#runs';
  private readonly INTERVAL_MS_SELECTOR = '#interval-ms';
  private readonly RUN_INVESTIGATION_SELECTOR = '#run-investigation';
  private readonly CLEAR_LOGS_SELECTOR = '#clear-logs';
  private readonly INVALIDATE_TOKEN_SELECTOR = '#invalidate-token';
  private readonly TOTAL_CALLS_SELECTOR = '#total-calls';
  private readonly SUCCESS_CALLS_SELECTOR = '#success-calls';
  private readonly FAIL_CALLS_SELECTOR = '#fail-calls';

  constructor(page: Page, dryRun: DryRun) {
    super(page, dryRun);
  }

  get pageNameKey() {
    return 'parallel-request';
  }

  async open() {
    await super.open('/parallel-request');
  }

  async setParallelCalls(count: number) {
    await this.inputText(this.PARALLEL_CALLS_SELECTOR, count);
  }

  async setRuns(count: number) {
    await this.inputText(this.RUNS_SELECTOR, count);
  }

  async setIntervalMs(ms: number) {
    await this.inputText(this.INTERVAL_MS_SELECTOR, ms);
  }

  async clickRunInvestigationButton() {
    await this.click(this.RUN_INVESTIGATION_SELECTOR);
  }

  async clickInvalidateAccessTokenButton() {
    await this.click(this.INVALIDATE_TOKEN_SELECTOR);
  }

  async clickClearLogsButton() {
    await this.click(this.CLEAR_LOGS_SELECTOR);
  }

  async expectSummary(total: number, success: number, fail: number) {
    await this.expectInnerText(this.TOTAL_CALLS_SELECTOR, String(total));
    await this.expectInnerText(this.SUCCESS_CALLS_SELECTOR, String(success));
    await this.expectInnerText(this.FAIL_CALLS_SELECTOR, String(fail));
  }
}
