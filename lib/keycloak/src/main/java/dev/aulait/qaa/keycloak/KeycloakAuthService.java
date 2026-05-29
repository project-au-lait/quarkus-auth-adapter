package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.LoginRequest;
import dev.aulait.qaa.api.MeResponse;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public class KeycloakAuthService {

  private final Keycloak keycloak;
  private final KeycloakTokenClient tokenClient;
  private final SecurityIdentity identity;
  private final AuthHttpClient authHttpClient;

  private final String realm;
  private final String clientId;
  private final String clientSecret;
  private final String tokenEndpoint;
  private final String authServerUrl;

  @Inject
  public KeycloakAuthService(
      @ConfigProperty(name = "auth.token-endpoint") String tokenEndpoint,
      @ConfigProperty(name = "auth.realm") String realm,
      @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url") String authServerUrl,
      @ConfigProperty(name = "quarkus.oidc.client-id") String clientId,
      @ConfigProperty(name = "quarkus.oidc.credentials.secret") String clientSecret,
      Keycloak keycloak,
      KeycloakTokenClient tokenClient,
      SecurityIdentity identity,
      AuthHttpClient authHttpClient) {
    this.keycloak = keycloak;
    this.tokenClient = tokenClient;
    this.identity = identity;
    this.authHttpClient = authHttpClient;
    this.realm = realm;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.tokenEndpoint = tokenEndpoint;
    this.authServerUrl = authServerUrl;
  }

  public AccessTokenResponse login(LoginRequest request, String dpopProof) {
    Map<String, String> params =
        withClientCredentials(
            Map.ofEntries(
                Map.entry("grant_type", "password"),
                Map.entry("username", request.getUserName()),
                Map.entry("password", request.getPassword())));
    return tokenClient.request(tokenEndpoint, params, dpopProof);
  }

  public AccessTokenResponse refresh(String refreshToken, String dpopProof) {
    Map<String, String> params =
        withClientCredentials(
            Map.ofEntries(
                Map.entry("grant_type", "refresh_token"),
                Map.entry("refresh_token", refreshToken)));
    return tokenClient.request(tokenEndpoint, params, dpopProof);
  }

  public MeResponse me() {
    if (identity.isAnonymous()) {
      return MeResponse.builder().build();
    }

    String username = identity.getPrincipal().getName();
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

  public boolean forgotPassword(String email) {
    List<UserRepresentation> users = keycloak.realm(realm).users().search(email, 0, 1);

    if (users.isEmpty()) {
      return false;
    }

    keycloak
        .realm(realm)
        .users()
        .get(users.get(0).getId())
        .executeActionsEmail(List.of("UPDATE_PASSWORD"));

    return true;
  }

  public boolean resetPassword(String code, String newPassword) {
    return authHttpClient.resetPassword(authServerUrl, realm, code, newPassword);
  }

  private Map<String, String> withClientCredentials(Map<String, String> grantParams) {
    var params = new HashMap<>(grantParams);
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);
    return Map.copyOf(params);
  }
}
