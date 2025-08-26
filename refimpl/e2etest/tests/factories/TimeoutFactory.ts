import 'dotenv/config';
export default class TimeoutFactory {
  static readonly SESSION_TIMEOUT = Number(process.env.PUBLIC_SESSION_TIMEOUT);
  private static readonly REFRESH_TOKEN_COOKIE_TIMEOUT =
    Number(process.env.REFRESH_TOKEN_COOKIE_TIMEOUT) * 1000;
  private static readonly ACCESS_TOKEN_TIMEOUT = Number(process.env.ACCESS_TOKEN_TIMEOUT) * 1000;
  private static readonly MARGIN = 300;
  static readonly TIMEOUT_WAIT_MULTIPLIER = 1.4;
  static readonly SESSION_MIDPOINT_OFFSET_RATIO = 0.6;
  static readonly TOKEN_REFRESH_INTERVAL = Number(process.env.PUBLIC_TOKEN_REFRESH_CHECK_INTERVAL);
  static readonly TOKEN_REFRESH_INTERVAL_MULTIPLIER = 1.2;

  static createTimeForLoggedOut(): number {
    return this.SESSION_TIMEOUT * this.TIMEOUT_WAIT_MULTIPLIER;
  }

  static createTimeBeyondSessionMidpoint(): number {
    return this.SESSION_TIMEOUT * this.SESSION_MIDPOINT_OFFSET_RATIO;
  }

  static createTimeForTokenRefresh(): number {
    return this.TOKEN_REFRESH_INTERVAL * this.TOKEN_REFRESH_INTERVAL_MULTIPLIER;
  }
  static refreshTokenTimeout() {
    return this.REFRESH_TOKEN_COOKIE_TIMEOUT + this.MARGIN;
  }

  static accessTokenTimeout() {
    return this.ACCESS_TOKEN_TIMEOUT + this.MARGIN;
  }
}
