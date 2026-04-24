package eu.alboranplus.chinvat.users.application.dto;

import java.util.Set;

public record UserView(
    Long id, String email, String displayName, Set<String> roles, boolean active) {

  public UserView {
    roles = Set.copyOf(roles);
  }
}
