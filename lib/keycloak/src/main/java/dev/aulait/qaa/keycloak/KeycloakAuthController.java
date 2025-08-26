package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.AuthController;
import dev.aulait.qaa.api.ForgotPasswordRequest;
import dev.aulait.qaa.api.LoginRequest;
import dev.aulait.qaa.api.LoginResponse;
import dev.aulait.qaa.api.ResetPasswordRequest;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

@Path(AuthController.BASE_PATH)
@Tags(@Tag(name = "Auth Controller"))
@RequiredArgsConstructor
public class KeycloakAuthController implements AuthController {

  private final AuthzClient authzClient;
  private final Keycloak keycloak;
  private final AuthHttpClient authHttpClient;

  @ConfigProperty(name = "auth.refreshToken.cookie.timeout")
  private int refreshTokenCookieTimeout;

  @Override
  public Response login(LoginRequest request) {
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
  public Response refreshToken(@CookieParam(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {

    if (refreshToken == null || refreshToken.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Refresh token is required")
          .build();
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
