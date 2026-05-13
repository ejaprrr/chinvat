package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Roles assigned to a user")
public record UserRolesResponse(
    @Schema(description = "User identifier", example = "550e8400-e29b-41d4-a716-446655440000") UUID userId,
    @Schema(description = "Assigned role names", example = "[\"USER\",\"EMPLOYEE\"]")
        Set<String> roles) {

  public UserRolesResponse {
    roles = Set.copyOf(roles);
  }
}
