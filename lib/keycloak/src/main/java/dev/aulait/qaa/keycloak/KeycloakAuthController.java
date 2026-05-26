package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.AuthController;
import dev.aulait.qaa.api.ForgotPasswordRequest;
import dev.aulait.qaa.api.LoginRequest;
import dev.aulait.qaa.api.LoginResponse;
import dev.aulait.qaa.api.MeResponse;
import dev.aulait.qaa.api.ResetPasswordRequest;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.keycloak.representations.AccessTokenResponse;

@Path(AuthController.BASE_PATH)
@Tags(@Tag(name = "Auth Controller"))
@RequiredArgsConstructor
public class KeycloakAuthController implements AuthController {

  private final KeycloakAuthService authService;

  @ConfigProperty(name = "auth.refreshToken.cookie.timeout")
  private int refreshTokenCookieTimeout;

  @Override
  public Response login(LoginRequest request, @HeaderParam(DPOP_HEADER_NAME) String dpopProof) {
    return toTokenResponse(authService.login(request, dpopProof));
  }

  @Override
  public MeResponse me() {
    return authService.me();
  }

  @Override
  public Response refreshToken(
      @CookieParam(REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
      @HeaderParam(DPOP_HEADER_NAME) String dpopProof) {

    if (refreshToken == null || refreshToken.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Refresh token is required")
          .build();
    }

    return toTokenResponse(authService.refresh(refreshToken, dpopProof));
  }

  @Override
  public Response forgotPassword(ForgotPasswordRequest request) {
    if (!authService.forgotPassword(request.getEmail())) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    return Response.ok().build();
  }

  @Override
  public Response resetPassword(ResetPasswordRequest request) {
    if (!authService.resetPassword(request.getCode(), request.getNewPassword())) {
      return Response.serverError().entity("Internal server error occurred").build();
    }
    return Response.ok().build();
  }

  protected Response toTokenResponse(AccessTokenResponse atr) {
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
}
