package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record AuthUserProjection(
    UUID userId, String email, String displayName, Set<String> roles, boolean active) {

  public AuthUserProjection {
    roles = Set.copyOf(roles);
  }
}
