package dev.aulait.qaa.qkref;

import static dev.aulait.qaa.api.AuthController.*;

import dev.aulait.qaa.api.ErrorResponse;
import dev.aulait.qaa.api.LoginRequest;
import dev.aulait.qaa.api.LoginResponse;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.Getter;

public class AuthClient {

  @Getter private RestClient client = new RestClient();

  public LoginResponse login(LoginRequest request) {
    Response response = client.given().body(request).post(BASE_PATH + LOGIN_PATH);

    return build(response);
  }

  LoginResponse build(Response response) {
    LoginResponse loginResponse = response.body().as(LoginResponse.class);
    client.setAccessToken(loginResponse.getAccessToken());
    client.setRefreshToken(response.getCookie(REFRESH_TOKEN_COOKIE_NAME));
    return loginResponse;
  }

  public ErrorResponse loginWithError(LoginRequest request) {
    return client.post(BASE_PATH + LOGIN_PATH, request, ErrorResponse.class);
  }

  public LoginResponse refreshToken() {
    Response response =
        client
            .given()
            .cookie(REFRESH_TOKEN_COOKIE_NAME, client.getRefreshToken())
            .get(BASE_PATH + REFRESH_TOKEN_PATH);

    return build(response);
  }

  public ErrorResponse refreshTokenWithError() {
    Response response =
        client
            .given()
            .cookie(REFRESH_TOKEN_COOKIE_NAME, "invalid-token")
            .get(BASE_PATH + REFRESH_TOKEN_PATH);

    Status status = Status.fromStatusCode(response.getStatusCode());

    return ErrorResponse.builder()
        .status(status.getReasonPhrase())
        .statusCode(status.getStatusCode())
        .build();
  }
}
