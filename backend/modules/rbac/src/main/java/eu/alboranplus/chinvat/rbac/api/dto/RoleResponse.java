package eu.alboranplus.chinvat.rbac.api.dto;

import java.util.Set;

public record RoleResponse(String roleName, Set<String> permissions) {

  public RoleResponse {
    permissions = Set.copyOf(permissions);
  }
}
