package dev.aulait.qaa.api;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
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

      Map<String, Object> jwk = new LinkedHashMap<>();
      jwk.put("kty", "EC");
      jwk.put("crv", "P-256");
      jwk.put("x", BASE64URL.encodeToString(x));
      jwk.put("y", BASE64URL.encodeToString(y));

      Map<String, Object> header = new LinkedHashMap<>();
      header.put("typ", "dpop+jwt");
      header.put("alg", "ES256");
      header.put("jwk", jwk);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("jti", UUID.randomUUID().toString());
      payload.put("htm", htm);
      payload.put("htu", htu);
      payload.put("iat", System.currentTimeMillis() / 1000);

      if (accessToken != null) {
        byte[] ath =
            MessageDigest.getInstance("SHA-256")
                .digest(accessToken.getBytes(StandardCharsets.UTF_8));
        payload.put("ath", BASE64URL.encodeToString(ath));
      }

      String encodedHeader =
          BASE64URL.encodeToString(toJson(header).getBytes(StandardCharsets.UTF_8));
      String encodedPayload =
          BASE64URL.encodeToString(toJson(payload).getBytes(StandardCharsets.UTF_8));
      String signingInput = encodedHeader + "." + encodedPayload;

      Signature sig = Signature.getInstance("SHA256withECDSAinP1363Format");
      sig.initSign(keyPair.getPrivate());
      sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
      byte[] signature = sig.sign();

      return signingInput + "." + BASE64URL.encodeToString(signature);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to build DPoP proof", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static String toJson(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (!first) sb.append(",");
      first = false;
      sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
      Object value = entry.getValue();
      if (value instanceof Map) {
        sb.append(toJson((Map<String, Object>) value));
      } else if (value instanceof Number) {
        sb.append(value);
      } else {
        sb.append("\"").append(escapeJson(value.toString())).append("\"");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  private static String escapeJson(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
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
