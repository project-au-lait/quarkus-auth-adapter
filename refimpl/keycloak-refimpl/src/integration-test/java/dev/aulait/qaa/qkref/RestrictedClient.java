package dev.aulait.qaa.qkref;

import dev.aulait.mousse.util.RestClient;
import java.net.http.HttpResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestrictedClient {

  private RestClient client;

  public String get() {
    return client.get(RestrictedController.BASE_PATH, String.class);
  }

  public HttpResponse<?> getAsRaw() {
    return client.get(RestrictedController.BASE_PATH, HttpResponse.class);
  }
}
