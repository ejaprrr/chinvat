package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Roles assigned to a user")
public record UserRolesResponse(
    @Schema(description = "User identifier", example = "42") Long userId,
    @Schema(description = "Assigned role names", example = "[\"USER\",\"EMPLOYEE\"]")
        Set<String> roles) {

  public UserRolesResponse {
    roles = Set.copyOf(roles);
  }
}
