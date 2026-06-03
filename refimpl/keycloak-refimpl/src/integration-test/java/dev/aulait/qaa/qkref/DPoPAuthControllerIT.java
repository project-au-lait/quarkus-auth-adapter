package dev.aulait.qaa.qkref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aulait.qaa.api.DPoPAuthClient;
import dev.aulait.qaa.api.LoginResponse;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
class DPoPAuthControllerIT {

  @Test
  void dpopLogin() {
    DPoPAuthClient client = new DPoPAuthClient();

    LoginResponse response = client.login(AuthDataFactory.createProvider1());

    assertNotNull(response.getAccessToken());
  }

  @Test
  void dpopRefreshToken() {
    DPoPAuthClient client = new DPoPAuthClient();

    LoginResponse loginResponse = client.login(AuthDataFactory.createProvider1());
    LoginResponse refreshResponse = client.refreshToken();

    assertNotEquals(loginResponse.getAccessToken(), refreshResponse.getAccessToken());
  }

  @Test
  void dpopRestrictedAccess() {
    DPoPAuthClient client = new DPoPAuthClient();
    client.login(AuthDataFactory.createProvider1());

    HttpResponse<String> response = client.getRestricted();

    assertEquals(200, response.statusCode());
    assertEquals(RestrictedController.GET_MESSAGE, response.body());
  }

  @Test
  void dpopRestrictedAccessWithoutProofRejected() {
    DPoPAuthClient client = new DPoPAuthClient();
    client.login(AuthDataFactory.createProvider1());

    HttpResponse<String> response = client.getRestrictedWithoutProof();

    assertEquals(401, response.statusCode());
  }

  @Test
  void dpopNonceRsRequiresNonce() {
    DPoPAuthClient client = new DPoPAuthClient();
    client.login(AuthDataFactory.createProvider1());

    // First request without nonce — RS should return 401 with DPoP-Nonce
    HttpResponse<String> response = client.getRestrictedWithoutNonceRetry();

    assertEquals(401, response.statusCode());

    String wwwAuth = response.headers().firstValue("WWW-Authenticate").orElse("");
    assertTrue(wwwAuth.contains("use_dpop_nonce"), "Expected use_dpop_nonce error");

    String nonce = response.headers().firstValue("DPoP-Nonce").orElse(null);
    assertNotNull(nonce, "Expected DPoP-Nonce header");

    // Retry with nonce
    HttpResponse<String> retryResponse = client.getRestrictedWithNonce(nonce);
    assertEquals(200, retryResponse.statusCode());
    assertEquals(RestrictedController.GET_MESSAGE, retryResponse.body());
  }

  @Test
  void dpopLoginInvalidCredentials() {
    DPoPAuthClient client = new DPoPAuthClient();

    HttpResponse<String> response =
        client.loginRaw(AuthDataFactory.createNonExistentUser());

    assertEquals(400, response.statusCode());
  }
}
