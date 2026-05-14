package eu.alboranplus.chinvat.profile.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Profile completion payload submitted after a successful eIDAS authentication")
public record CompleteEidasProfileRequest(
    @Schema(description = "eIDAS provider code", example = "EIDAS_EU")
        @NotBlank String providerCode,
    @Schema(description = "External subject identifier from the eIDAS callback", example = "ES/ES/12345678A")
        @NotBlank String externalSubjectId,
    @Schema(description = "Unique username", example = "alice_smith")
        @NotBlank @Size(max = 100) String username,
    @Schema(description = "Full name", example = "Alice Smith")
        @NotBlank @Size(max = 255) String fullName,
    @Schema(description = "Phone number", example = "+34 612 345 678")
        @Size(max = 40) String phoneNumber,
    @Schema(description = "Email address", example = "alice@example.com")
        @NotBlank @Email String email,
    @Schema(description = "Password — min 12, max 128 characters", example = "S3cur3P@ssw0rd!!")
        @NotBlank @Size(min = 12, max = 128) String password,
    @Schema(description = "Address line", example = "Calle Mayor 1")
        @Size(max = 255) String addressLine,
    @Schema(description = "Postal code", example = "29001")
        @Size(max = 20) String postalCode,
    @Schema(description = "City", example = "Malaga")
        @Size(max = 100) String city,
    @Schema(description = "Country", example = "Spain")
        @Size(max = 100) String country,
    @Schema(description = "Default language code (IETF)", example = "es")
        @NotBlank @Size(max = 12) String defaultLanguage,
    @Schema(description = "Optional PEM-encoded X.509 certificate to bind during registration")
        String certificatePem,
    @Schema(description = "Certificate assurance level", example = "HIGH")
        @Size(max = 80) String assuranceLevel,
    @Schema(description = "Certificate provider code", example = "FNMT")
        @Size(max = 80) String certificateProviderCode,
    @Schema(description = "Identity reference (NIF/NIE/passport number)", example = "12345678A")
        @Size(max = 255) String identityReference,
    @Schema(description = "Nationality ISO 3166-1 alpha-2 code", example = "ES")
        @Size(max = 80) String nationality) {}
