package eu.alboranplus.chinvat.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Login credentials")
public record LoginRequest(
    @Schema(description = "User email address", example = "alice@example.com")
        @NotBlank
        @Email
        String email,
    @Schema(description = "User password", example = "S3cur3P@ssw0rd!!")
        @NotBlank
        @Size(min = 1, max = 128)
        String password) {}
