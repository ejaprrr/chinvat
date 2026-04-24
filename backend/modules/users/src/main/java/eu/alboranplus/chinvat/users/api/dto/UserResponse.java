package eu.alboranplus.chinvat.users.api.dto;

import java.util.Set;

public record UserResponse(
    Long id, String email, String displayName, Set<String> roles, boolean active) {

  public UserResponse {
    roles = Set.copyOf(roles);
  }
}
