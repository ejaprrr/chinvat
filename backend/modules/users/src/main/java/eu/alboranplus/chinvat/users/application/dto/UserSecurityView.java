package eu.alboranplus.chinvat.users.application.dto;

import java.util.Set;

public record UserSecurityView(
    Long id, String email, String displayName, Set<String> roles, boolean active) {

  public UserSecurityView {
    roles = Set.copyOf(roles);
  }
}
