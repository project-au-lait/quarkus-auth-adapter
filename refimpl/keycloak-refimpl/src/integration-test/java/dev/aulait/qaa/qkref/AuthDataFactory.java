package dev.aulait.qaa.qkref;

import dev.aulait.qaa.api.LoginRequest;

public class AuthDataFactory {

  public static LoginRequest createProvider1() {
    return LoginRequest.builder().userName("provider-1").password("provider-1").build();
  }

  public static LoginRequest createNonExistentUser() {
    return LoginRequest.builder()
        .userName("non-existent-user")
        .password("non-existent-password")
        .build();
  }
}
