package eu.alboranplus.chinvat.rbac.application.dto;

import java.util.Set;
import java.util.UUID;

public record UserRolesView(UUID userId, Set<String> roles) {

  public UserRolesView {
    roles = Set.copyOf(roles);
  }
}
