package eu.alboranplus.chinvat.users.api.dto;

import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Update user payload")
public record UpdateUserRequest(
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
    @Schema(description = "User type (INDIVIDUAL or LIBRARY)")
        @NotNull
        UserType userType,
    @Schema(description = "Access level")
        @NotNull
        AccessLevel accessLevel,
    @Schema(description = "Address line", example = "Calle Mayor 1")
        @Size(max = 255)
        String addressLine,
    @Schema(description = "Postal code", example = "29001")
        @Size(max = 20)
        String postalCode,
    @Schema(description = "City", example = "Malaga")
        @Size(max = 100)
        String city,
    @Schema(description = "Country", example = "Spain")
        @Size(max = 100)
        String country,
    @Schema(description = "Default language code", example = "es")
        @NotBlank
        @Size(max = 12)
        String defaultLanguage) {}
