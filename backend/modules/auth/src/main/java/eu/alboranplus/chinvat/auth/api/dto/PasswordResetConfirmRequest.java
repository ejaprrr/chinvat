package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Confirm password reset using a reset token")
public record PasswordResetConfirmRequest(
    @Schema(description = "Password reset token", example = "P:...") @NotBlank String resetToken,
    @Schema(description = "New password", example = "N3wS3cur3P@ssw0rd!")
        @NotBlank
        @Size(min = 12, max = 128)
        String newPassword) {}

