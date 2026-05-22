package dev.aulait.qaa.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class MeResponse {
  @Schema(required = true)
  private String firstName;

  @Schema(required = true)
  private String lastName;

  @Schema(required = true)
  private List<String> roles;
}
