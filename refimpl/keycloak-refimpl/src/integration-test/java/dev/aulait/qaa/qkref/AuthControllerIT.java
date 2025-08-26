package dev.aulait.qaa.qkref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dev.aulait.qaa.api.ErrorResponse;
import dev.aulait.qaa.api.LoginResponse;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
class AuthControllerIT {

  AuthClient authClient = new AuthClient();
  RestrictedClient restrictedClient = new RestrictedClient(authClient.getClient());

  @Test
  void restrictedAccess() {
    authClient.login(AuthDataFactory.createProvider1());

    String response = restrictedClient.get();

    assertEquals(RestrictedController.GET_MESSAGE, response);
  }

  @Test
  void tokenRefresh() {
    LoginResponse loginResponse1 = authClient.login(AuthDataFactory.createProvider1());

    LoginResponse loginResponse2 = authClient.refreshToken();

    assertNotEquals(loginResponse1.getAccessToken(), loginResponse2.getAccessToken());

    String response = restrictedClient.get();

    assertEquals(RestrictedController.GET_MESSAGE, response);
  }

  @Test
  void accessTokenTimeout() throws InterruptedException {
    authClient.login(AuthDataFactory.createProvider1());

    Thread.sleep(TestConfig.getInstance().getAccessTokenTimeout() * 1000L);

    ErrorResponse error = restrictedClient.getAsError();

    assertEquals(Status.UNAUTHORIZED.getStatusCode(), error.getStatusCode());
  }

  @Test
  void loginError() {
    ErrorResponse error = authClient.loginWithError(AuthDataFactory.createNonExistentUser());

    assertEquals(Status.UNAUTHORIZED.getStatusCode(), error.getStatusCode());
  }

  @Test
  void illegalRefreshToken() {
    ErrorResponse error = authClient.refreshTokenWithError();

    assertEquals(Status.BAD_REQUEST.getStatusCode(), error.getStatusCode());
  }
}
