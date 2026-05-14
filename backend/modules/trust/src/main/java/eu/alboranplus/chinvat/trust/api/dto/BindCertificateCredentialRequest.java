package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Payload for binding a certificate credential to a user account")
public record BindCertificateCredentialRequest(
    @Schema(description = "Target user UUID", type = "string", format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "userId is required") UUID userId,
    @Schema(description = "Certificate provider code", example = "FNMT")
        @NotBlank(message = "providerCode is required") String providerCode,
    @Schema(description = "Source of this credential registration", example = "ADMIN_BIND")
        @NotBlank(message = "registrationSource is required") String registrationSource,
    @Schema(description = "Assurance level", example = "HIGH")
        String assuranceLevel,
    @Schema(description = "PEM-encoded X.509 certificate to bind")
        @NotBlank(message = "certificatePem is required") String certificatePem) {}
