package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Authenticated user profile (/auth/me)")
public record AuthMeResponse(
    @Schema(description = "User id", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
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

