package dev.aulait.qaa.qkref;

import dev.aulait.qaa.api.ErrorResponse;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

public class RestClient {

  @Getter @Setter private String accessToken;
  @Getter @Setter private String refreshToken;

  public RequestSpecification given() {
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(TestConfig.getInstance().getBaseUrl())
            .contentType("application/json; charset=UTF-8")
            .header("Accept-Language", Locale.getDefault().toString().replace("_", "-"))
            .filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    if (accessToken != null) {
      spec.header("Authorization", "Bearer " + accessToken);
    }

    return spec;
  }

  public <T> T get(String path, Class<T> responseType, Object... pathParams) {
    return as(given().get(path, pathParams).then().extract(), responseType);
  }

  public <T> T post(String path, Object requestBody, Class<T> responseType, Object... pathParams) {
    return as(given().body(requestBody).post(path, pathParams).then().extract(), responseType);
  }

  public <T> T put(String path, Object requestBody, Class<T> responseType, Object... pathParams) {
    return as(given().body(requestBody).put(path, pathParams).then().extract(), responseType);
  }

  public <T> T delete(
      String path, Object requestBody, Class<T> responseType, Object... pathParams) {
    return as(given().body(requestBody).delete(path, pathParams).then().extract(), responseType);
  }

  @SuppressWarnings("unchecked")
  private <T> T as(ExtractableResponse<Response> response, Class<T> responseType) {
    if (responseType == String.class) {
      return (T) response.asString();

    } else if (responseType == Integer.class) {
      return (T) Integer.valueOf(response.asString());

    } else if (responseType == ErrorResponse.class) {
      ErrorResponse error =
          ContentType.JSON.matches(response.contentType())
              ? response.as(ErrorResponse.class)
              : new ErrorResponse();

      Status status = Status.fromStatusCode(response.statusCode());
      error.setStatus(status.getReasonPhrase());
      error.setStatusCode(status.getStatusCode());

      return (T) error;

    } else {
      return response.as(responseType);
    }
  }
}
