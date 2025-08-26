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
public class ErrorResponse {
  @Schema(required = true)
  private int statusCode;

  @Schema(required = true)
  private String status;

  @Schema(required = true)
  private String title;

  @Schema(required = true)
  private String detail;
}
