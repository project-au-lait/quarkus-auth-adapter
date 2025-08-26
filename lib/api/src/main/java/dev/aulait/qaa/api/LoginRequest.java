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
public class LoginRequest {

  @Schema(required = true)
  private String userName;

  @Schema(required = true)
  private String password;
}
