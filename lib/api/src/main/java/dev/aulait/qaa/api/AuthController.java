package dev.aulait.qaa.api;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

public interface AuthController {

  static final String BASE_PATH = "auth";
  static final String LOGIN_PATH = "/login";
  static final String FORGOT_PASSWORD_PATH = "/forgot-password";
  static final String RESET_PASSWORD_PATH = "/reset-password";
  static final String REFRESH_TOKEN_PATH = "/refresh-token";
  static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @POST
  @Path(LOGIN_PATH)
  @APIResponse(
      responseCode = "200",
      description = "Returns access token and sets refresh token as a cookie",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = LoginResponse.class)))
  Response login(LoginRequest request);

  @GET
  @Path(REFRESH_TOKEN_PATH)
  @APIResponse(
      responseCode = "200",
      description = "Returns access token and sets refresh token as a cookie",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = LoginResponse.class)))
  Response refreshToken(@CookieParam(REFRESH_TOKEN_COOKIE_NAME) String refreshToken);

  @POST
  @Path(FORGOT_PASSWORD_PATH)
  Response forgotPassword(ForgotPasswordRequest request);

  @POST
  @Path(RESET_PASSWORD_PATH)
  Response resetPassword(ResetPasswordRequest request);
}
