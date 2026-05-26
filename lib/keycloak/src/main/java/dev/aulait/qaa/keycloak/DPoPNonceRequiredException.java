package dev.aulait.qaa.keycloak;

public class DPoPNonceRequiredException extends RuntimeException {

  private final String nonce;

  public DPoPNonceRequiredException(String nonce) {
    super("DPoP nonce required");
    this.nonce = nonce;
  }

  public String getNonce() {
    return nonce;
  }
}
