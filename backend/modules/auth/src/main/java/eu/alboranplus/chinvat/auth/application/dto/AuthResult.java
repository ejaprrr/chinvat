package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;
import java.util.UUID;

public record AuthResult(
    UUID userId,
    String email,
    String displayName,
    Set<String> roles,
    Set<String> permissions,
    IssuedTokenPair tokens) {

  public AuthResult {
    roles = Set.copyOf(roles);
    permissions = Set.copyOf(permissions);
  }
}
