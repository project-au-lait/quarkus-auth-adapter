package dev.aulait.qaa.qkref;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path(RestrictedController.BASE_PATH)
public class RestrictedController {

  static final String BASE_PATH = "/restricted";

  public static final String GET_MESSAGE = "This is a message from the restricted controller";

  @GET
  public String get() {
    return GET_MESSAGE;
  }
}
