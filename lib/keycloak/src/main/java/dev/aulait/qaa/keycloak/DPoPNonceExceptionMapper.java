package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.AuthController;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DPoPNonceExceptionMapper implements ExceptionMapper<DPoPNonceRequiredException> {

  @Override
  public Response toResponse(DPoPNonceRequiredException e) {
    return Response.status(Response.Status.UNAUTHORIZED)
        .header(AuthController.DPOP_NONCE_HEADER_NAME, e.getNonce())
        .build();
  }
}
