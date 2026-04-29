package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Change the current user's password using the existing password")
public record PasswordChangeRequest(
    @Schema(description = "Current password", example = "Curr3ntP@ssw0rd!")
        @NotBlank
        @Size(min = 1, max = 128)
        String currentPassword,
    @Schema(description = "New password", example = "N3wS3cur3P@ssw0rd!")
        @NotBlank
        @Size(min = 12, max = 128)
        String newPassword) {}