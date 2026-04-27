package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Logout payload — both tokens are required to fully revoke the session")
public record LogoutRequest(
    @Schema(description = "Current access token") @NotBlank String accessToken,
    @Schema(description = "Current refresh token") @NotBlank String refreshToken) {}
