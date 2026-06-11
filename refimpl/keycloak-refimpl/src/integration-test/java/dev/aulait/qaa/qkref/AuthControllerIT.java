package dev.aulait.qaa.qkref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aulait.qaa.api.AuthClient;
import dev.aulait.qaa.api.ErrorResponse;
import dev.aulait.qaa.api.LoginResponse;
import dev.aulait.qaa.api.MeResponse;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import jakarta.ws.rs.core.Response.Status;
import java.net.http.HttpResponse;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
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

    Config config = ConfigProvider.getConfig();
    int accessTokenTimeout =
        config.getOptionalValue("auth.accessToken.timeout", Integer.class).orElse(10);
    Thread.sleep(accessTokenTimeout * 1000L);

    HttpResponse<?> response = restrictedClient.getAsRaw();

    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.statusCode());
  }

  @Test
  void loginError() {
    ErrorResponse error = authClient.loginWithError(AuthDataFactory.createNonExistentUser());

    assertEquals(Status.BAD_REQUEST.getStatusCode(), error.getStatusCode());
  }

  @Test
  void illegalRefreshToken() {
    ErrorResponse error = authClient.refreshTokenWithError();

    assertEquals(Status.BAD_REQUEST.getStatusCode(), error.getStatusCode());
  }

  @Test
  void me_authenticated() {
    authClient.login(AuthDataFactory.createProvider1());

    MeResponse me = authClient.me();

    assertEquals("ProviderFirstName", me.getFirstName());
    assertEquals("ProviderLastName", me.getLastName());
    assertTrue(me.getRoles().contains("provider"));
  }

  @Test
  void me_unauthenticated() {
    AuthClient unauthenticatedClient = new AuthClient();

    MeResponse me = unauthenticatedClient.me();

    assertNull(me.getFirstName());
    assertNull(me.getLastName());
    assertNull(me.getRoles());
  }

  @Test
  void logout() {
    authClient.login(AuthDataFactory.createProvider1());
    authClient.logout();

    assertNull(authClient.getRefreshTokenCookie());
  }
}
