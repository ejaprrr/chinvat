package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;

public record TokenPrincipal(Long userId, String email, Set<String> roles, Set<String> permissions) {

  public TokenPrincipal {
    roles = Set.copyOf(roles);
    permissions = Set.copyOf(permissions);
  }
}
