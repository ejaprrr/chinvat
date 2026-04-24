package eu.alboranplus.chinvat.rbac.domain.model;

import java.util.Set;

public record RoleDefinition(String roleName, Set<String> permissions) {

  public RoleDefinition {
    if (roleName == null || roleName.isBlank()) {
      throw new IllegalArgumentException("Role name must not be blank");
    }

    roleName = roleName.trim().toUpperCase();
    permissions = Set.copyOf(permissions);
  }
}
