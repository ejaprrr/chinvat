package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetRequestResponse(
    @Schema(
        description = "Optional reset token (returned only in local/test profile)",
        nullable = true)
        String resetToken) {}

