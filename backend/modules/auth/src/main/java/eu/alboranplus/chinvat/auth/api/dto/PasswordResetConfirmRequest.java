package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Confirm password reset using email and a short numeric reset code")
public record PasswordResetConfirmRequest(
    @Schema(description = "User email", example = "alice@example.com")
        @NotBlank
        @Email
        String email,
    @Schema(description = "Six-digit password reset code", example = "482193")
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "Reset code must contain exactly 6 digits")
        String resetCode,
    @Schema(description = "New password", example = "N3wS3cur3P@ssw0rd!")
        @NotBlank
        @Size(min = 12, max = 128)
        String newPassword) {}

