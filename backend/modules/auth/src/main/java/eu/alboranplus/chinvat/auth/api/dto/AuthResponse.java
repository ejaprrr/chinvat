package eu.alboranplus.chinvat.auth.api.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(UserInfo user, TokenInfo tokens) {

  public record UserInfo(
      Long id, String email, String displayName, Set<String> roles, Set<String> permissions) {

    public UserInfo {
      roles = Set.copyOf(roles);
      permissions = Set.copyOf(permissions);
    }
  }

  public record TokenInfo(String accessToken, String refreshToken, Instant expiresAt) {}
}
