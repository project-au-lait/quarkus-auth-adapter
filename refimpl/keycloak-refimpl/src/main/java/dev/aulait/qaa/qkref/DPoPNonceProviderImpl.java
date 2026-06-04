package dev.aulait.qaa.qkref;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.quarkus.oidc.DPoPNonceProvider;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DPoPNonceProviderImpl implements DPoPNonceProvider {

  @ConfigProperty(name = "qaa.dpop.nonce-ttl-seconds")
  long nonceTtlSeconds;

  private final SecureRandom secureRandom = new SecureRandom();
  private Cache<String, Boolean> validNonces;

  @PostConstruct
  void init() {
    validNonces = Caffeine.newBuilder()
        .expireAfterWrite(nonceTtlSeconds, TimeUnit.SECONDS)
        .build();
  }

  @Override
  public String getNonce() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    validNonces.put(nonce, Boolean.TRUE);
    return nonce;
  }

  @Override
  public boolean isValid(String nonce) {
    if (nonce == null) {
      return false;
    }
    return validNonces.getIfPresent(nonce) != null;
  }
}
