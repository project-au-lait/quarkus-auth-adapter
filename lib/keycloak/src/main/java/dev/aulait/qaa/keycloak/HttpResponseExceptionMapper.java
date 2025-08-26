package dev.aulait.qaa.keycloak;

import dev.aulait.qaa.api.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.authorization.client.util.HttpResponseException;

@Provider
public class HttpResponseExceptionMapper implements ExceptionMapper<HttpResponseException> {

  @Override
  public Response toResponse(HttpResponseException exception) {
    ErrorResponse response = new ErrorResponse();

    Status status = Status.fromStatusCode(exception.getStatusCode());
    response.setStatus(status.getReasonPhrase());
    response.setStatusCode(status.getStatusCode());
    response.setTitle(exception.getReasonPhrase());
    response.setDetail(exception.getMessage());

    return Response.status(response.getStatusCode()).entity(response).build();
  }
}
