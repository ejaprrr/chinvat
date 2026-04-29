package eu.alboranplus.chinvat.rbac.application.dto;

import java.util.Set;

public record UserRolesView(Long userId, Set<String> roles) {

  public UserRolesView {
    roles = Set.copyOf(roles);
  }
}
