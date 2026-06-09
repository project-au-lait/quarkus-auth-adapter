package dev.aulait.qaa.api;

import static dev.aulait.qaa.api.AuthController.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class DPoPAuthClient {

  private static final Pattern ACCESS_TOKEN_PATTERN =
      Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

  @Getter private String accessToken;
  @Getter private final DPoPProofBuilder proofBuilder = new DPoPProofBuilder();
  private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .cookieHandler(cookieManager)
          .connectTimeout(Duration.ofMillis(5000L))
          .build();
  private final String baseUrl;
  private final String tokenEndpoint;

  public DPoPAuthClient() {
    Config config = ConfigProvider.getConfig();
    int port = config.getOptionalValue("quarkus.http.test-port", Integer.class).orElse(8080);
    String restPath = config.getOptionalValue("quarkus.rest.path", String.class).orElse("");
    this.baseUrl = "http://localhost:" + port + restPath;
    this.tokenEndpoint =
        config
            .getOptionalValue("auth.token-endpoint", String.class)
            .orElse("http://localhost:8085/realms/qaa-realm/protocol/openid-connect/token");
  }

  public LoginResponse login(LoginRequest request) {
    String url = baseUrl + "/" + BASE_PATH + LOGIN_PATH;
    String dpopProof = proofBuilder.buildProof("POST", tokenEndpoint);

    HttpResponse<String> response = doPost(url, request, dpopProof);

    if (response.statusCode() == 200) {
      accessToken = extractAccessToken(response.body());
      return LoginResponse.builder().accessToken(accessToken).build();
    }
    throw new RuntimeException(
        "DPoP login failed: " + response.statusCode() + " " + response.body());
  }

  public HttpResponse<String> loginRaw(LoginRequest request) {
    String url = baseUrl + "/" + BASE_PATH + LOGIN_PATH;
    String dpopProof = proofBuilder.buildProof("POST", tokenEndpoint);
    return doPost(url, request, dpopProof);
  }

  public HttpResponse<String> loginRawWithNonce(LoginRequest request, String nonce) {
    String url = baseUrl + "/" + BASE_PATH + LOGIN_PATH;
    String dpopProof = proofBuilder.buildProof("POST", tokenEndpoint, null, nonce);
    return doPost(url, request, dpopProof);
  }

  public LoginResponse refreshToken() {
    String url = baseUrl + "/" + BASE_PATH + REFRESH_TOKEN_PATH;
    String dpopProof = proofBuilder.buildProof("POST", tokenEndpoint);

    HttpResponse<String> response = doGet(url, dpopProof);

    if (response.statusCode() == 200) {
      accessToken = extractAccessToken(response.body());
      return LoginResponse.builder().accessToken(accessToken).build();
    }
    throw new RuntimeException(
        "DPoP refresh failed: " + response.statusCode() + " " + response.body());
  }

  public HttpResponse<String> refreshTokenRaw() {
    String url = baseUrl + "/" + BASE_PATH + REFRESH_TOKEN_PATH;
    String dpopProof = proofBuilder.buildProof("POST", tokenEndpoint);
    return doGet(url, dpopProof);
  }

  public HttpResponse<String> getRestricted() {
    String url = baseUrl + "/restricted";
    String dpopProof = proofBuilder.buildProof("GET", url, accessToken);
    HttpResponse<String> response = doGetWithAuth(url, dpopProof);

    // Auto-retry with nonce if RS requires it
    if (response.statusCode() == 401) {
      String nonce = response.headers().firstValue("DPoP-Nonce").orElse(null);
      if (nonce != null) {
        String retryProof = proofBuilder.buildProof("GET", url, accessToken, nonce);
        return doGetWithAuth(url, retryProof);
      }
    }
    return response;
  }

  public HttpResponse<String> getRestrictedWithoutNonceRetry() {
    String url = baseUrl + "/restricted";
    String dpopProof = proofBuilder.buildProof("GET", url, accessToken);
    return doGetWithAuth(url, dpopProof);
  }

  public HttpResponse<String> getRestrictedWithNonce(String nonce) {
    String url = baseUrl + "/restricted";
    String dpopProof = proofBuilder.buildProof("GET", url, accessToken, nonce);
    return doGetWithAuth(url, dpopProof);
  }

  public HttpResponse<String> getRestrictedWithoutProof() {
    String url = baseUrl + "/restricted";
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "DPoP " + accessToken)
            .GET()
            .build();
    return send(request);
  }

  private HttpResponse<String> doPost(String url, LoginRequest loginRequest, String dpopProof) {
    String body = toJson(loginRequest);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("DPoP", dpopProof)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return send(request);
  }

  private HttpResponse<String> doGet(String url, String dpopProof) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("DPoP", dpopProof)
            .GET()
            .build();
    return send(request);
  }

  private HttpResponse<String> doGetWithAuth(String url, String dpopProof) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "DPoP " + accessToken)
            .header("DPoP", dpopProof)
            .GET()
            .build();
    return send(request);
  }

  private HttpResponse<String> send(HttpRequest request) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Request interrupted", e);
    } catch (Exception e) {
      throw new RuntimeException("Request failed", e);
    }
  }

  private String toJson(LoginRequest request) {
    return "{\"userName\":\"" + escapeJson(request.getUserName())
        + "\",\"password\":\"" + escapeJson(request.getPassword()) + "\"}";
  }

  private String extractAccessToken(String json) {
    Matcher m = ACCESS_TOKEN_PATTERN.matcher(json);
    if (m.find()) {
      return m.group(1);
    }
    throw new RuntimeException("accessToken not found in response: " + json);
  }

  private static String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
