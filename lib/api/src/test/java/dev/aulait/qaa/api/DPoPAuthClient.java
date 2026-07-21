package dev.aulait.qaa.api;

import static dev.aulait.qaa.api.AuthController.*;

import dev.aulait.mousse.util.JsonUtils;
import dev.aulait.mousse.util.RestClient;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Function;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class DPoPAuthClient {

  @Getter private String accessToken;
  @Getter private final DPoPProofBuilder proofBuilder = new DPoPProofBuilder();
  private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .cookieHandler(cookieManager)
          .connectTimeout(Duration.ofMillis(5000L))
          .build();

  private String dpopProof;
  private boolean useDpopAuthorization;

  @Getter
  private final RestClient client =
      RestClient.builder()
          .httpClient(httpClient)
          .quarkus()
          .allowNonSuccessStatus(true)
          .headerSupplier("DPoP", () -> dpopProof)
          .headerSupplier(
              "Authorization",
              () -> useDpopAuthorization && accessToken != null ? "DPoP " + accessToken : null)
          .build();

  private final String tokenEndpoint;

  public DPoPAuthClient() {
    Config config = ConfigProvider.getConfig();
    this.tokenEndpoint =
        config
            .getOptionalValue("auth.token-endpoint", String.class)
            .orElse("http://localhost:8085/realms/qaa-realm/protocol/openid-connect/token");
  }

  public LoginResponse login(LoginRequest request) {
    String proof = proofBuilder.buildProof("POST", tokenEndpoint);
    LoginResponse loginResponse =
        post(BASE_PATH + LOGIN_PATH, tokenEndpoint, request, LoginResponse.class, proof, false);
    accessToken = loginResponse.getAccessToken();
    return loginResponse;
  }

  public HttpResponse<String> loginWithRawResponse(LoginRequest request) {
    String proof = proofBuilder.buildProof("POST", tokenEndpoint);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response =
        (HttpResponse<String>)
            post(BASE_PATH + LOGIN_PATH, tokenEndpoint, request, HttpResponse.class, proof, false);
    return response;
  }

  public LoginResponse tokenRefresh() {
    String proof = proofBuilder.buildProof("POST", tokenEndpoint);
    LoginResponse loginResponse =
        get(BASE_PATH + REFRESH_TOKEN_PATH, tokenEndpoint, LoginResponse.class, proof, false);
    accessToken = loginResponse.getAccessToken();
    return loginResponse;
  }

  public MeResponse me() {
    String path = BASE_PATH + ME_PATH;
    String proof = proofBuilder.buildProof("GET", absoluteUrlFor(path), accessToken, null);
    return get(path, path, MeResponse.class, proof, true);
  }

  public HttpResponse<String> getRestricted() {
    String restrictedPath = "/restricted";
    String proof =
        proofBuilder.buildProof("GET", absoluteUrlFor(restrictedPath), accessToken, null);
    return get(restrictedPath, restrictedPath, HttpResponse.class, proof, true);
  }

  public HttpResponse<String> getRestrictedWithoutNonceAndRetry() {
    String restrictedPath = "/restricted";
    String proofWithoutNonce =
        proofBuilder.buildProof("GET", absoluteUrlFor(restrictedPath), accessToken, null);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response =
        (HttpResponse<String>)
            getWithoutRetry(restrictedPath, HttpResponse.class, proofWithoutNonce, true);
    return response;
  }

  public HttpResponse<String> getRestrictedWithNonce(String nonce) {
    String restrictedPath = "/restricted";
    String proof =
        proofBuilder.buildProof("GET", absoluteUrlFor(restrictedPath), accessToken, nonce);
    @SuppressWarnings("unchecked")
    HttpResponse<String> response =
        (HttpResponse<String>) getWithoutRetry(restrictedPath, HttpResponse.class, proof, true);
    return response;
  }

  public HttpResponse<String> getRestrictedWithoutProof() {
    @SuppressWarnings("unchecked")
    HttpResponse<String> response =
        (HttpResponse<String>) getWithoutRetry("/restricted", HttpResponse.class, null, true);
    return response;
  }

  public <T> T get(
      String path, String htu, Class<T> responseType, String dpopProof, boolean useDpopAuthorization) {
    return executeWithDpopNonceRetry(
        "GET",
        htu,
        responseType,
        dpopProof,
        useDpopAuthorization,
        proof -> {
          @SuppressWarnings("unchecked")
          HttpResponse<String> response =
              (HttpResponse<String>)
                  getWithoutRetry(path, HttpResponse.class, proof, useDpopAuthorization);
          return response;
        });
  }

  public <T> T post(
      String path,
      String htu,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    return executeWithDpopNonceRetry(
        "POST",
        htu,
        responseType,
        dpopProof,
        useDpopAuthorization,
        proof -> {
          @SuppressWarnings("unchecked")
          HttpResponse<String> response =
              (HttpResponse<String>)
                  postWithoutRetry(
                      path, request, HttpResponse.class, proof, useDpopAuthorization);
          return response;
        });
  }

  public <T> T put(
      String path,
      String htu,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    return executeWithDpopNonceRetry(
        "PUT",
        htu,
        responseType,
        dpopProof,
        useDpopAuthorization,
        proof -> {
          @SuppressWarnings("unchecked")
          HttpResponse<String> response =
              (HttpResponse<String>)
                  putWithoutRetry(
                      path, request, HttpResponse.class, proof, useDpopAuthorization);
          return response;
        });
  }

  public <T> T delete(
      String path,
      String htu,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    return executeWithDpopNonceRetry(
        "DELETE",
        htu,
        responseType,
        dpopProof,
        useDpopAuthorization,
        proof -> {
          @SuppressWarnings("unchecked")
          HttpResponse<String> response =
              (HttpResponse<String>)
                  deleteWithoutRetry(
                      path, request, HttpResponse.class, proof, useDpopAuthorization);
          return response;
        });
  }

  private <T> T executeWithDpopNonceRetry(
      String method,
      String htu,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization,
      Function<String, HttpResponse<String>> executor) {
    HttpResponse<String> rawResponse = executor.apply(dpopProof);

    if (requiresDpopNonceRetry(rawResponse)) {
      String nonce = rawResponse.headers().firstValue("DPoP-Nonce").orElse(null);
      if (nonce != null) {
        String retryProof =
            proofBuilder.buildProof(
                  method, absoluteUrlFor(htu), useDpopAuthorization ? accessToken : null, nonce);
        rawResponse = executor.apply(retryProof);
      }
    }

    return toResponseType(rawResponse, responseType);
  }

  private <T> T getWithoutRetry(
      String path, Class<T> responseType, String dpopProof, boolean useDpopAuthorization) {
    this.dpopProof = dpopProof;
    this.useDpopAuthorization = useDpopAuthorization;
    try {
      return client.get(path, responseType);
    } finally {
      clearRequestScopedHeaders();
    }
  }

  private <T> T postWithoutRetry(
      String path,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    this.dpopProof = dpopProof;
    this.useDpopAuthorization = useDpopAuthorization;
    try {
      return client.post(path, request, responseType);
    } finally {
      clearRequestScopedHeaders();
    }
  }

  private <T> T putWithoutRetry(
      String path,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    this.dpopProof = dpopProof;
    this.useDpopAuthorization = useDpopAuthorization;
    try {
      return client.put(path, request, responseType);
    } finally {
      clearRequestScopedHeaders();
    }
  }

  private <T> T deleteWithoutRetry(
      String path,
      Object request,
      Class<T> responseType,
      String dpopProof,
      boolean useDpopAuthorization) {
    this.dpopProof = dpopProof;
    this.useDpopAuthorization = useDpopAuthorization;
    try {
      return client.delete(path, request, responseType);
    } finally {
      clearRequestScopedHeaders();
    }
  }

  private boolean requiresDpopNonceRetry(HttpResponse<?> response) {
    if (response.statusCode() == 400) {
      return true;
    }
    if (response.statusCode() != 401) {
      return false;
    }

    String wwwAuthenticate = response.headers().firstValue("WWW-Authenticate").orElse("");
    return wwwAuthenticate.contains("use_dpop_nonce");
  }

  @SuppressWarnings("unchecked")
  private <T> T toResponseType(HttpResponse<String> response, Class<T> responseType) {
    if (responseType == HttpResponse.class) {
      return (T) response;
    }
    if (responseType == String.class) {
      return (T) response.body();
    }
    return JsonUtils.str2obj(response.body(), responseType);
  }

  private String absoluteUrlFor(String path) {
    if (path.startsWith("http://") || path.startsWith("https://")) {
        return path;
    }
    String normalizedPath = path.startsWith("/") ? path : "/" + path;
    String base = client.getBaseUrl();
    if (base.endsWith("/")) {
        base = base.substring(0, base.length() - 1);
    }
    return base + normalizedPath;
  }

  private void clearRequestScopedHeaders() {
    dpopProof = null;
    useDpopAuthorization = false;
  }
}
