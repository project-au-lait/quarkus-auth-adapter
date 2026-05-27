package dev.aulait.qaa.qkref;

import dev.aulait.qaa.api.AuthClient;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestrictedClient {

  private AuthClient authClient;

  public String get() {
    authClient.prepareDPoP("GET", resolveUrl());
    return authClient.getClient().get(RestrictedController.BASE_PATH, String.class);
  }

  public HttpResponse<?> getAsRaw() {
    authClient.prepareDPoP("GET", resolveUrl());
    return authClient.getClient().get(RestrictedController.BASE_PATH, HttpResponse.class);
  }

  private String resolveUrl() {
    String base = authClient.getClient().getBaseUrl();
    String path = RestrictedController.BASE_PATH;
    if (base.endsWith("/") && path.startsWith("/")) {
      return base + path.substring(1);
    }
    return base + path;
  }
}
