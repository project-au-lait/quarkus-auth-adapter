package dev.aulait.qaa.api;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.UUID;

public class DPoPProofBuilder {

  private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

  private final KeyPair keyPair;

  public DPoPProofBuilder() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
      gen.initialize(new ECGenParameterSpec("secp256r1"));
      this.keyPair = gen.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate EC key pair", e);
    }
  }

  public String build(String htm, String htu, String accessToken) {
    try {
      ECPublicKey pub = (ECPublicKey) keyPair.getPublic();
      byte[] x = toUnsignedBytes(pub.getW().getAffineX(), 32);
      byte[] y = toUnsignedBytes(pub.getW().getAffineY(), 32);

      String header =
          "{\"typ\":\"dpop+jwt\",\"alg\":\"ES256\",\"jwk\":"
              + "{\"kty\":\"EC\",\"crv\":\"P-256\""
              + ",\"x\":\""
              + BASE64URL.encodeToString(x)
              + "\""
              + ",\"y\":\""
              + BASE64URL.encodeToString(y)
              + "\"}}";

      StringBuilder payload = new StringBuilder();
      payload
          .append("{\"jti\":\"")
          .append(UUID.randomUUID())
          .append("\",\"htm\":\"")
          .append(htm)
          .append("\",\"htu\":\"")
          .append(htu)
          .append("\",\"iat\":")
          .append(System.currentTimeMillis() / 1000);

      if (accessToken != null) {
        byte[] ath = MessageDigest.getInstance("SHA-256").digest(accessToken.getBytes("UTF-8"));
        payload.append(",\"ath\":\"").append(BASE64URL.encodeToString(ath)).append("\"");
      }

      payload.append("}");

      String encodedHeader = BASE64URL.encodeToString(header.getBytes("UTF-8"));
      String encodedPayload = BASE64URL.encodeToString(payload.toString().getBytes("UTF-8"));
      String signingInput = encodedHeader + "." + encodedPayload;

      Signature sig = Signature.getInstance("SHA256withECDSAinP1363Format");
      sig.initSign(keyPair.getPrivate());
      sig.update(signingInput.getBytes("UTF-8"));
      byte[] signature = sig.sign();

      return signingInput + "." + BASE64URL.encodeToString(signature);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to build DPoP proof", e);
    }
  }

  private static byte[] toUnsignedBytes(BigInteger value, int length) {
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
