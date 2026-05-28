package dev.aulait.qaa.api;

public class UrlUtils {

  private UrlUtils() {}

  public static String resolve(String baseUrl, String path) {
    if (baseUrl.endsWith("/") && path.startsWith("/")) {
      return baseUrl + path.substring(1);
    }
    return baseUrl + path;
  }
}
