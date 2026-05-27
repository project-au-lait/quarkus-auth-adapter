package dev.aulait.qaa.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.AccessTokenResponse;

@ApplicationScoped
@RequiredArgsConstructor
public class KeycloakTokenClient {

  private final ObjectMapper objectMapper;

  public AccessTokenResponse request(
      String tokenEndpoint, Map<String, String> params, String dpopProof) {
    String formBody = buildFormBody(params);
    HttpRequest httpRequest = buildHttpRequest(tokenEndpoint, formBody, dpopProof);
    return send(httpRequest);
  }

  private String buildFormBody(Map<String, String> params) {
    return params.entrySet().stream()
        .map(
            e ->
                URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }

  private HttpRequest buildHttpRequest(String tokenEndpoint, String formBody, String dpopProof) {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(tokenEndpoint))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formBody));

    if (dpopProof != null && !dpopProof.isEmpty()) {
      builder.header("DPoP", dpopProof);
    }

    return builder.build();
  }

  private AccessTokenResponse send(HttpRequest httpRequest) {
    try (HttpClient client = HttpClient.newHttpClient()) {
      HttpResponse<String> response =
          client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 401) {
        String nonce = response.headers().firstValue("DPoP-Nonce").orElse(null);
        if (nonce != null) {
          throw new DPoPNonceRequiredException(nonce);
        }
      }

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new HttpResponseException(
            "Token request failed",
            response.statusCode(),
            response.body(),
            response.body().getBytes(StandardCharsets.UTF_8));
      }

      return objectMapper.readValue(response.body(), AccessTokenResponse.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Token request interrupted", e);
    }
  }
}
