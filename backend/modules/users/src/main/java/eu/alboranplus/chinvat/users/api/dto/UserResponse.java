package eu.alboranplus.chinvat.users.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User account data")
public record UserResponse(
    @Schema(description = "Internal user ID", example = "42") Long id,
    @Schema(description = "Username", example = "alice_smith") String username,
    @Schema(description = "Full name", example = "Alice Smith") String fullName,
    @Schema(description = "Phone number", example = "+34 612 345 678") String phoneNumber,
    @Schema(description = "Email address", example = "alice@example.com") String email,
    @Schema(description = "User type", example = "INDIVIDUAL") String userType,
    @Schema(description = "Access level", example = "NORMAL") String accessLevel,
    @Schema(description = "Address line") String addressLine,
    @Schema(description = "Postal code") String postalCode,
    @Schema(description = "City") String city,
    @Schema(description = "Country") String country,
    @Schema(description = "Default language code", example = "es") String defaultLanguage) {}

