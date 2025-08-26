package dev.aulait.qaa.qkref;

import static dev.aulait.qaa.qkref.RestrictedController.*;

import dev.aulait.qaa.api.ErrorResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestrictedClient {

  private RestClient client;

  public String get() {
    return client.get(BASE_PATH, String.class);
  }

  public ErrorResponse getAsError() {
    return client.get(BASE_PATH, ErrorResponse.class);
  }
}
