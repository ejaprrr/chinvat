package eu.alboranplus.chinvat.profile.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Certificate credential associated with the authenticated user's profile")
public record ProfileCertificateResponse(
    @Schema(description = "Credential UUID", type = "string", format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
    @Schema(description = "Provider code", example = "FNMT") String providerCode,
    @Schema(description = "Trust status", example = "TRUSTED") String trustStatus,
    @Schema(description = "Revocation status", example = "VALID") String revocationStatus,
    @Schema(description = "Assurance level", example = "HIGH") String assuranceLevel,
    @Schema(description = "SHA-256 thumbprint of the certificate") String thumbprintSha256,
    @Schema(description = "Subject distinguished name") String subjectDn,
    @Schema(description = "Issuer distinguished name") String issuerDn,
    @Schema(description = "Certificate serial number") String serialNumber,
    @Schema(description = "Certificate valid-from timestamp (UTC)") Instant notBefore,
    @Schema(description = "Certificate valid-until timestamp (UTC)") Instant notAfter,
    @Schema(description = "True if this is the user's primary certificate") boolean primary,
    @Schema(description = "Record creation timestamp (UTC)") Instant createdAt,
    @Schema(description = "Record last-updated timestamp (UTC)") Instant updatedAt) {}
