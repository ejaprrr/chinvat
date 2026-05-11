package eu.alboranplus.chinvat.users.application.dto;

import java.util.Set;
import java.util.UUID;

public record UserSecurityView(
    UUID id, String email, String displayName, Set<String> roles, boolean active) {

  public UserSecurityView {
    roles = Set.copyOf(roles);
  }
}
