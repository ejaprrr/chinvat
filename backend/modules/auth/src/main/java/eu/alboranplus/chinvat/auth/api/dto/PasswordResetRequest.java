package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request a password reset")
public record PasswordResetRequest(
    @Schema(description = "User email", example = "alice@example.com")
        @NotBlank
        @Email
        String email) {}

