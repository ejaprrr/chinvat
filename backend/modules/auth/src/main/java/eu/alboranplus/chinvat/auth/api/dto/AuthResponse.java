package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;

@Schema(description = "Authentication response containing user info and issued token pair")
public record AuthResponse(
    @Schema(description = "Authenticated user details") UserInfo user,
    @Schema(description = "Issued token pair") TokenInfo tokens) {

  @Schema(description = "Authenticated user profile")
  public record UserInfo(
      @Schema(description = "Internal user ID", example = "42") Long id,
      @Schema(description = "Email address", example = "alice@example.com") String email,
      @Schema(description = "Display name", example = "Alice Smith") String displayName,
      @Schema(description = "Set of assigned role names", example = "[\"USER\"]") Set<String> roles,
      @Schema(description = "Set of resolved permissions", example = "[\"READ_PROFILE\"]") Set<String> permissions) {

    public UserInfo {
      roles = Set.copyOf(roles);
      permissions = Set.copyOf(permissions);
    }
  }

  @Schema(description = "Issued token pair")
  public record TokenInfo(
      @Schema(description = "Short-lived opaque access token") String accessToken,
      @Schema(description = "Long-lived opaque refresh token") String refreshToken,
      @Schema(description = "Access token expiry timestamp (UTC)") Instant expiresAt) {}
}
