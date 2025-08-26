package dev.aulait.qaa.keycloak;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;

@ApplicationScoped
public class AuthHttpClient {

  public boolean resetPassword(
      String authServerUrl, String realm, String code, String newPassword) {

    String relayPageUrl =
        String.format("%s/realms/%s/login-actions/action-token?key=%s", authServerUrl, realm, code);

    CookieManager cookieManager = new InsecureCookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    HttpClient httpClient =
        HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    try {
      HttpResponse<String> relayPageResponse = executeGet(httpClient, relayPageUrl);
      String resetFormUrl = Jsoup.parse(relayPageResponse.body()).selectFirst("a").absUrl("href");

      HttpResponse<String> resetFormResponse = executeGet(httpClient, resetFormUrl);
      String resetActionUrl =
          Jsoup.parse(resetFormResponse.body()).selectFirst("form").absUrl("action");

      String formBody =
          String.format(
              "password-new=%s&password-confirm=%s",
              URLEncoder.encode(newPassword, StandardCharsets.UTF_8),
              URLEncoder.encode(newPassword, StandardCharsets.UTF_8));

      HttpResponse<String> postResponse = executePost(httpClient, resetActionUrl, formBody);

      return postResponse.statusCode() == HttpURLConnection.HTTP_OK;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      return false;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private HttpResponse<String> executeGet(HttpClient client, String url)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> executePost(HttpClient client, String url, String body)
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }
}
