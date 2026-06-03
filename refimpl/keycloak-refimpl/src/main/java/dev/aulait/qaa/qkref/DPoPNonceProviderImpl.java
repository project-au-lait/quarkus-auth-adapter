package dev.aulait.qaa.qkref;

import io.quarkus.oidc.DPoPNonceProvider;
import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DPoPNonceProviderImpl implements DPoPNonceProvider {

  private static final int NONCE_BYTE_LENGTH = 22;
  private static final SecureRandom RANDOM = new SecureRandom();
  private final Set<String> validNonces = ConcurrentHashMap.newKeySet();

  private volatile String currentNonce = generateNonce();

  @Override
  public String getNonce() {
    return currentNonce;
  }

  @Override
  public boolean isValid(String nonce) {
    return nonce != null && validNonces.contains(nonce);
  }

  private String generateNonce() {
    byte[] bytes = new byte[NONCE_BYTE_LENGTH];
    RANDOM.nextBytes(bytes);
    String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    validNonces.add(nonce);
    return nonce;
  }
}
