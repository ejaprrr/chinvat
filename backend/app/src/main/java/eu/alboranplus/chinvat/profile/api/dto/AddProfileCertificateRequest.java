package eu.alboranplus.chinvat.profile.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for adding a certificate credential to the authenticated user's profile")
public record AddProfileCertificateRequest(
    @Schema(description = "PEM-encoded X.509 certificate to bind")
        @NotBlank String certificatePem,
    @Schema(description = "Certificate provider code", example = "FNMT")
        @Size(max = 80) String providerCode,
    @Schema(description = "Assurance level", example = "HIGH")
        @Size(max = 80) String assuranceLevel) {}
