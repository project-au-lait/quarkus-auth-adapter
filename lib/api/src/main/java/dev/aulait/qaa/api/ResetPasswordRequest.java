package dev.aulait.qaa.api;

import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
public class ResetPasswordRequest {

  @Schema(required = true)
  private String code;

  @Schema(required = true)
  private String newPassword;
}
