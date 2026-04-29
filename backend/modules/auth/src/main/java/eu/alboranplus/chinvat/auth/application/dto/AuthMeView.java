package eu.alboranplus.chinvat.auth.application.dto;

import java.util.Set;

public record AuthMeView(
    Long id, String email, String displayName, Set<String> roles, Set<String> permissions) {
  public AuthMeView {
    roles = Set.copyOf(roles);
    permissions = Set.copyOf(permissions);
  }
}

