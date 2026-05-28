package dev.aulait.qaa.qkref;

import dev.aulait.qaa.api.AuthClient;
import dev.aulait.qaa.api.UrlUtils;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestrictedClient {

  private AuthClient authClient;

  public String get() {
    authClient.prepareDPoPWithAuth(
        "GET",
        UrlUtils.resolve(authClient.getClient().getBaseUrl(), RestrictedController.BASE_PATH));
    return authClient.getClient().get(RestrictedController.BASE_PATH, String.class);
  }

  public HttpResponse<?> getAsRaw() {
    authClient.prepareDPoPWithAuth(
        "GET",
        UrlUtils.resolve(authClient.getClient().getBaseUrl(), RestrictedController.BASE_PATH));
    return authClient.getClient().get(RestrictedController.BASE_PATH, HttpResponse.class);
  }
}
