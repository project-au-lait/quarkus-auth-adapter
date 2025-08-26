package dev.aulait.qaa.keycloak;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsecureCookieManager extends CookieManager {
  @Override
  public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
    if ("http".equalsIgnoreCase(uri.getScheme()) && "localhost".equalsIgnoreCase(uri.getHost())) {
      List<String> modifiedCookies =
          responseHeaders.getOrDefault("Set-Cookie", List.of()).stream()
              .map(cookie -> cookie.replaceAll(";\\s*Secure", ""))
              .toList();

      Map<String, List<String>> modifiedHeaders = new HashMap<>(responseHeaders);
      modifiedHeaders.put("Set-Cookie", modifiedCookies);

      super.put(uri, modifiedHeaders);
    } else {
      super.put(uri, responseHeaders);
    }
  }
}
