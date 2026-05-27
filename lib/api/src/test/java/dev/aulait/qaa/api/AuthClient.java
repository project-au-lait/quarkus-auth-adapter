package dev.aulait.qaa.api;

import static dev.aulait.qaa.api.AuthController.*;

import dev.aulait.mousse.util.RestClient;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import lombok.Getter;

public class AuthClient {

  @Getter private String accessToken;
  private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
  private HttpClient httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();

  @Getter
  private RestClient client =
      RestClient.builder()
          .httpClient(httpClient)
          .quarkus()
          .allowNonSuccessStatus(true)
          .headerSupplier(
              "Authorization", () -> accessToken != null ? "Bearer " + accessToken : null)
          .build();

  public LoginResponse login(LoginRequest request) {
    LoginResponse response = client.post(BASE_PATH + LOGIN_PATH, request, LoginResponse.class);
    accessToken = response.getAccessToken();
    return response;
  }

  public ErrorResponse loginWithError(LoginRequest request) {
    return client.post(BASE_PATH + LOGIN_PATH, request, ErrorResponse.class);
  }

  public MeResponse me() {
    return client.get(BASE_PATH + ME_PATH, MeResponse.class);
  }

  public LoginResponse refreshToken() {
    return client.get(BASE_PATH + REFRESH_TOKEN_PATH, LoginResponse.class);
  }

  public ErrorResponse refreshTokenWithError() {
    URI baseUri = URI.create(client.getBaseUrl());

    HttpCookie cookie = new HttpCookie(REFRESH_TOKEN_COOKIE_NAME, "invalid-token");
    cookie.setDomain(baseUri.getHost());
    cookie.setPath("/");
    cookie.setVersion(0);

    cookieManager.getCookieStore().add(baseUri, cookie);

    return client.get(BASE_PATH + REFRESH_TOKEN_PATH, ErrorResponse.class);
  }
}
