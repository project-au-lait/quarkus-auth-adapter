package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.AuthController;
import dev.aulait.qaa.api.ForgotPasswordRequest;
import dev.aulait.qaa.api.LoginRequest;
import dev.aulait.qaa.api.LoginResponse;
import dev.aulait.qaa.api.MeResponse;
import dev.aulait.qaa.api.ResetPasswordRequest;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

@Path(AuthController.BASE_PATH)
@Tags(@Tag(name = "Auth Controller"))
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthController implements AuthController {

  private final AuthzClient authzClient;
  private final Keycloak keycloak;
  private final AuthHttpClient authHttpClient;
  private final SecurityIdentity identity;

  private static final String DPOP_REQUEST_FAILED_MSG = "DPoP token request failed";
  private static final String DPOP_NONCE_HEADER_NAME = "DPoP-Nonce";

  @ConfigProperty(name = "auth.refreshToken.cookie.timeout")
  private int refreshTokenCookieTimeout;

  @ConfigProperty(name = "auth.token-endpoint")
  private Optional<String> tokenEndpointConfig;

  @Override
  public Response login(LoginRequest request, @HeaderParam("DPoP") String dpopProof) {
    if (dpopProof != null && !dpopProof.isEmpty()) {
      String formBody =
          clientCredentials()
              + "&grant_type=password"
              + "&username=" + encode(request.getUserName())
              + "&password=" + encode(request.getPassword());
      return postWithDPoP(formBody, dpopProof);
    }

    AccessTokenResponse atr =
        authzClient.obtainAccessToken(request.getUserName(), request.getPassword());

    return build(atr);
  }

  protected Response build(AccessTokenResponse atr) {
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setAccessToken(atr.getToken());

    NewCookie cookie =
        new NewCookie.Builder(REFRESH_TOKEN_COOKIE_NAME)
            .value(atr.getRefreshToken())
            .maxAge(refreshTokenCookieTimeout)
            .httpOnly(true)
            .build();

    return Response.ok(loginResponse).cookie(cookie).build();
  }

  @Override
  public MeResponse me() {
    if (identity.isAnonymous()) {
      return MeResponse.builder().build();
    }

    String username = identity.getPrincipal().getName();
    String realm = authzClient.getConfiguration().getRealm();
    List<UserRepresentation> users = keycloak.realm(realm).users().search(username, 0, 1);

    if (users.isEmpty()) {
      return MeResponse.builder().build();
    }

    UserRepresentation user = users.get(0);
    return MeResponse.builder()
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .roles(List.copyOf(identity.getRoles()))
        .build();
  }

  @Override
  public Response refreshToken(
      @CookieParam(REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
      @HeaderParam("DPoP") String dpopProof) {

    if (refreshToken == null || refreshToken.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Refresh token is required")
          .build();
    }

    if (dpopProof != null && !dpopProof.isEmpty()) {
      String formBody =
          clientCredentials()
              + "&grant_type=refresh_token"
              + "&refresh_token=" + encode(refreshToken);
      return postWithDPoP(formBody, dpopProof);
    }

    Configuration config = authzClient.getConfiguration();
    String tokenEndpoint = authzClient.getServerConfiguration().getTokenEndpoint();
    Http http = new Http(config, config.getClientCredentialsProvider());

    AccessTokenResponse atr =
        http.<AccessTokenResponse>post(tokenEndpoint)
            .authentication()
            .client()
            .form()
            .param("grant_type", "refresh_token")
            .param("refresh_token", refreshToken)
            .response()
            .json(AccessTokenResponse.class)
            .execute();

    return build(atr);
  }

  private Response postWithDPoP(String formBody, String dpopProof) {
    try {
      String tokenEndpoint = resolveTokenEndpoint();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(tokenEndpoint))
              .header("Content-Type", "application/x-www-form-urlencoded")
              .header("DPoP", dpopProof)
              .POST(HttpRequest.BodyPublishers.ofString(formBody))
              .build();

      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        AccessTokenResponse atr =
            JsonSerialization.readValue(response.body(), AccessTokenResponse.class);
        return build(atr);
      }

      String dpopNonce = response.headers().firstValue(DPOP_NONCE_HEADER_NAME).orElse(null);
      Response.ResponseBuilder rb =
          Response.status(response.statusCode())
              .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
              .entity(response.body());
      if (dpopNonce != null) {
        rb.header(DPOP_NONCE_HEADER_NAME, dpopNonce);
      }
      return rb.build();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("DPoP token request interrupted", e);
      return Response.serverError().entity(DPOP_REQUEST_FAILED_MSG).build();
    } catch (Exception e) {
      log.error(DPOP_REQUEST_FAILED_MSG, e);
      return Response.serverError().entity(DPOP_REQUEST_FAILED_MSG).build();
    }
  }

  private String clientCredentials() {
    Configuration config = authzClient.getConfiguration();
    return "client_id=" + encode(config.getResource())
        + "&client_secret=" + encode((String) config.getCredentials().get("secret"));
  }

  private String resolveTokenEndpoint() {
    return tokenEndpointConfig
        .filter(s -> !s.isEmpty())
        .orElseGet(() -> authzClient.getServerConfiguration().getTokenEndpoint());
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @Override
  public Response forgotPassword(ForgotPasswordRequest request) {
    String realm = authzClient.getConfiguration().getRealm();
    List<UserRepresentation> users = keycloak.realm(realm).users().search(request.getEmail(), 0, 1);

    if (users.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    keycloak
        .realm(realm)
        .users()
        .get(users.get(0).getId())
        .executeActionsEmail(List.of("UPDATE_PASSWORD"));

    return Response.ok().build();
  }

  public Response resetPassword(ResetPasswordRequest request) {
    boolean result =
        authHttpClient.resetPassword(
            authzClient.getConfiguration().getAuthServerUrl(),
            authzClient.getConfiguration().getRealm(),
            request.getCode(),
            request.getNewPassword());

    if (!result) {
      return Response.serverError().entity("Internal server error occurred").build();
    }

    return Response.ok().build();
  }
}
