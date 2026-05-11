package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record TokenPrincipal(UUID userId, String email, Set<String> roles, Set<String> permissions) {

  public TokenPrincipal {
    roles = Set.copyOf(roles);
    permissions = Set.copyOf(permissions);
  }
}
