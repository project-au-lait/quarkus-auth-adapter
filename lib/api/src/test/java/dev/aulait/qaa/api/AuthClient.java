package dev.aulait.qaa.api;

import static dev.aulait.qaa.api.AuthController.*;

import dev.aulait.mousse.util.RestClient;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class AuthClient {

  @Getter private String accessToken;
  private final DPoPProofBuilder dpopProofBuilder = new DPoPProofBuilder();
  private String nextHtm;
  private String nextHtu;
  private boolean nextIncludeAuth = true;

  @Getter private final String tokenEndpoint;

  private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
  private HttpClient httpClient =
      HttpClient.newBuilder()
          .cookieHandler(cookieManager)
          .connectTimeout(Duration.ofMillis(5000L))
          .build();

  @Getter private RestClient client;

  public AuthClient() {
    Config config = ConfigProvider.getConfig();
    String authPort = config.getOptionalValue("auth.port", String.class).orElse("8085");
    String authRealm = config.getOptionalValue("auth.realm", String.class).orElse("qaa-realm");
    tokenEndpoint =
        "http://localhost:"
            + authPort
            + "/realms/"
            + authRealm
            + "/protocol/openid-connect/token";

    client =
        RestClient.builder()
            .httpClient(httpClient)
            .quarkus()
            .allowNonSuccessStatus(true)
            .headerSupplier(
                "Authorization",
                () -> nextIncludeAuth && accessToken != null ? "DPoP " + accessToken : null)
            .headerSupplier(
                "DPoP",
                () ->
                    dpopProofBuilder.build(
                        nextHtm, nextHtu, nextIncludeAuth ? accessToken : null))
            .build();
  }

  public void prepareDPoP(String htm, String htu) {
    this.nextHtm = htm;
    this.nextHtu = htu;
    this.nextIncludeAuth = true;
  }

  public LoginResponse login(String username, String password) {
    return login(new LoginRequest(username, password));
  }

  public LoginResponse login(LoginRequest request) {
    nextHtm = "POST";
    nextHtu = tokenEndpoint;
    nextIncludeAuth = true;
    LoginResponse response = client.post(BASE_PATH + LOGIN_PATH, request, LoginResponse.class);
    accessToken = response.getAccessToken();
    return response;
  }

  public ErrorResponse loginWithError(String username, String password) {
    return loginWithError(new LoginRequest(username, password));
  }

  public ErrorResponse loginWithError(LoginRequest request) {
    nextHtm = "POST";
    nextHtu = tokenEndpoint;
    nextIncludeAuth = true;
    return client.post(BASE_PATH + LOGIN_PATH, request, ErrorResponse.class);
  }

  public MeResponse me() {
    nextHtm = "GET";
    nextHtu = resolveUrl(BASE_PATH + ME_PATH);
    nextIncludeAuth = true;
    return client.get(BASE_PATH + ME_PATH, MeResponse.class);
  }

  public LoginResponse refreshToken() {
    nextHtm = "POST";
    nextHtu = tokenEndpoint;
    nextIncludeAuth = false;
    LoginResponse response = client.get(BASE_PATH + REFRESH_TOKEN_PATH, LoginResponse.class);
    accessToken = response.getAccessToken();
    return response;
  }

  public ErrorResponse refreshTokenWithError() {
    URI baseUri = URI.create(client.getBaseUrl());

    HttpCookie cookie = new HttpCookie(REFRESH_TOKEN_COOKIE_NAME, "invalid-token");
    cookie.setDomain(baseUri.getHost());
    cookie.setPath("/");
    cookie.setVersion(0);

    cookieManager.getCookieStore().add(baseUri, cookie);

    nextHtm = "POST";
    nextHtu = tokenEndpoint;
    nextIncludeAuth = false;
    return client.get(BASE_PATH + REFRESH_TOKEN_PATH, ErrorResponse.class);
  }

  private String resolveUrl(String path) {
    String base = client.getBaseUrl();
    if (base.endsWith("/") && path.startsWith("/")) {
      return base + path.substring(1);
    }
    return base + path;
  }
}
