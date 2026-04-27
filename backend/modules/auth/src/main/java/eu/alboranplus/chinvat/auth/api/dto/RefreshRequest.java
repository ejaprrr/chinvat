package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Token refresh payload")
public record RefreshRequest(
    @Schema(description = "Valid refresh token issued at login or from a previous refresh")
        @NotBlank
        String refreshToken) {}
