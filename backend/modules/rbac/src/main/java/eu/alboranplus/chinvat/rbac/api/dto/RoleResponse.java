package eu.alboranplus.chinvat.rbac.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Role definition including all associated permissions")
public record RoleResponse(
    @Schema(description = "Unique role name", example = "ADMIN") String roleName,
    @Schema(description = "Set of permissions granted by this role",
            example = "[\"READ_PROFILE\",\"MANAGE_USERS\"]") Set<String> permissions) {

  public RoleResponse {
    permissions = Set.copyOf(permissions);
  }
}
