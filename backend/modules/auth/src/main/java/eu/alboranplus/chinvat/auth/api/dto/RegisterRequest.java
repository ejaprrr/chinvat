package eu.alboranplus.chinvat.auth.api.dto;

import eu.alboranplus.chinvat.users.domain.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Register a new user")
public record RegisterRequest(
    @Schema(description = "Unique username", example = "alice_smith")
        @NotBlank
        @Size(max = 100)
        String username,
    @Schema(description = "Full name", example = "Alice Smith")
        @NotBlank
        @Size(max = 255)
        String fullName,
    @Schema(description = "Phone number", example = "+34 612 345 678")
        @Size(max = 40)
        String phoneNumber,
    @Schema(description = "Email address", example = "alice@example.com")
        @NotBlank
        @Email
        String email,
    @Schema(description = "Password", example = "S3cur3P@ssw0rd!!")
        @NotBlank
        @Size(min = 12, max = 128)
        String password,
    @Schema(description = "User type")
        @NotNull
        UserType userType,
    @Schema(description = "Address line")
        @Size(max = 255)
        String addressLine,
    @Schema(description = "Postal code")
        @Size(max = 20)
        String postalCode,
    @Schema(description = "City")
        @Size(max = 100)
        String city,
    @Schema(description = "Country")
        @Size(max = 100)
        String country,
    @Schema(description = "Default language code", example = "en")
        @NotBlank
        @Size(max = 12)
        String defaultLanguage) {}

