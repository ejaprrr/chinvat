package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;

public record AuthUserProjection(
    Long userId, String email, String displayName, Set<String> roles, boolean active) {

  public AuthUserProjection {
    roles = Set.copyOf(roles);
  }
}
