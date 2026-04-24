package eu.alboranplus.chinvat.rbac.application.dto;

import java.util.Set;

public record RoleView(String roleName, Set<String> permissions) {

  public RoleView {
    permissions = Set.copyOf(permissions);
  }
}
