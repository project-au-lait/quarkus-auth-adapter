package dev.aulait.qaa.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LoginResponse {
  @Schema(required = true)
  private String accessToken;
}
