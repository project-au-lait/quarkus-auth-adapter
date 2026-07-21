package dev.aulait.qaa.api;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.UUID;

public class DPoPProofBuilder {

  private final KeyPair keyPair;

  public DPoPProofBuilder() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
      kpg.initialize(new ECGenParameterSpec("secp256r1"));
      this.keyPair = kpg.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate EC key pair", e);
    }
  }

  public String buildProof(String htm, String htu, String accessToken, String nonce) {
    try {
      String header = buildHeader();
      String payload = buildPayload(htm, htu, accessToken, nonce);

      String encodedHeader = base64url(header.getBytes(StandardCharsets.UTF_8));
      String encodedPayload = base64url(payload.getBytes(StandardCharsets.UTF_8));
      String signingInput = encodedHeader + "." + encodedPayload;

      Signature sig = Signature.getInstance("SHA256withECDSAinP1363Format");
      sig.initSign(keyPair.getPrivate());
      sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
      byte[] signature = sig.sign();

      return signingInput + "." + base64url(signature);
    } catch (Exception e) {
      throw new RuntimeException("Failed to build DPoP proof", e);
    }
  }

  public String buildProof(String htm, String htu, String accessToken) {
    return buildProof(htm, htu, accessToken, null);
  }

  public String buildProof(String htm, String htu) {
    return buildProof(htm, htu, null, null);
  }

  private String buildHeader() {
    ECPublicKey pub = (ECPublicKey) keyPair.getPublic();
    byte[] x = toUnsignedBytes(pub.getW().getAffineX(), 32);
    byte[] y = toUnsignedBytes(pub.getW().getAffineY(), 32);

    return "{\"typ\":\"dpop+jwt\",\"alg\":\"ES256\",\"jwk\":{"
        + "\"kty\":\"EC\",\"crv\":\"P-256\","
        + "\"x\":\"" + base64url(x) + "\","
        + "\"y\":\"" + base64url(y) + "\"}}";
  }

  private String buildPayload(String htm, String htu, String accessToken, String nonce) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"jti\":\"").append(UUID.randomUUID()).append("\"");
    sb.append(",\"htm\":\"").append(htm).append("\"");
    sb.append(",\"htu\":\"").append(htu).append("\"");
    sb.append(",\"iat\":").append(System.currentTimeMillis() / 1000);

    if (accessToken != null) {
      sb.append(",\"ath\":\"").append(computeAth(accessToken)).append("\"");
    }
    if (nonce != null) {
      sb.append(",\"nonce\":\"").append(nonce).append("\"");
    }

    sb.append("}");
    return sb.toString();
  }

  private String computeAth(String accessToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(accessToken.getBytes(StandardCharsets.US_ASCII));
      return base64url(hash);
    } catch (Exception e) {
      throw new RuntimeException("Failed to compute ath", e);
    }
  }

  private static String base64url(byte[] data) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
  }

  private static byte[] toUnsignedBytes(java.math.BigInteger value, int length) {
    byte[] bytes = value.toByteArray();
    if (bytes.length == length) {
      return bytes;
    } else if (bytes.length == length + 1 && bytes[0] == 0) {
      byte[] result = new byte[length];
      System.arraycopy(bytes, 1, result, 0, length);
      return result;
    } else {
      byte[] result = new byte[length];
      System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
      return result;
    }
  }
}
