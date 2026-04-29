package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Authenticated user profile (/auth/me)")
public record AuthMeResponse(
    @Schema(description = "User id", example = "42") Long id,
    @Schema(description = "Email address", example = "alice@example.com") String email,
    @Schema(description = "Display name", example = "Alice Smith") String displayName,
    @Schema(description = "Assigned role names", example = "[\"USER\"]") Set<String> roles,
    @Schema(description = "Resolved permissions", example = "[\"PROFILE:READ\"]")
        Set<String> permissions) {
  public AuthMeResponse {
    roles = Set.copyOf(roles);
    permissions = Set.copyOf(permissions);
  }
}

