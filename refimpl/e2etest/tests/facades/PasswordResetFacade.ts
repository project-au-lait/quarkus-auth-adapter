import { DryRun } from '@arch/DryRun';
import MailpitTopPage from '@pages/MailpitTopPage';
import ResetPasswordPage from '@pages/ResetPasswordPage';
import TopPage from '@pages/TopPage';
import { Browser } from '@playwright/test';

class PasswordResetFacade {
  async resetPassword(browser: Browser, email: string, newPassword: string) {
    const contextTop = await browser.newContext();
    const pageTop = await contextTop.newPage();
    const dryRunTop = DryRun.build();

    const topPage = new TopPage(pageTop, dryRunTop);
    await topPage.open();
    const forgotPasswordPage = await topPage.gotoForgotPasswordPage();

    await forgotPasswordPage.submit(email);

    const contextMailpitTop = await browser.newContext();
    const pageMailpitTop = await contextMailpitTop.newPage();
    const dryRunMailpitTop = DryRun.build();

    const mailpitTopPage = new MailpitTopPage(pageMailpitTop, dryRunMailpitTop);
    await mailpitTopPage.open();
    const mailpitViewPage = await mailpitTopPage.gotoViewPage();
    const resetUrl = await mailpitViewPage.getResetPageUrl();
    await mailpitViewPage.deleteEmailMessage();

    const contextReset = await browser.newContext();
    const pageReset = await contextReset.newPage();
    const dryRunReset = DryRun.build();

    const resetPasswordPage = new ResetPasswordPage(pageReset, dryRunReset);
    await resetPasswordPage.open(resetUrl);
    await resetPasswordPage.resetPassword(newPassword);
  }
}

const passwordResetFacade = new PasswordResetFacade();
export default passwordResetFacade;
