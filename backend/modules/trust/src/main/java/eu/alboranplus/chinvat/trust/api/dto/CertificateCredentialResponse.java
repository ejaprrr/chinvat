package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Certificate credential record (admin view)")
public record CertificateCredentialResponse(
    @Schema(description = "Credential UUID", type = "string", format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
    @Schema(description = "Owner user UUID", type = "string", format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440001") UUID userId,
    @Schema(description = "Provider code", example = "FNMT") String providerCode,
    @Schema(description = "Credential type", example = "PERSONAL") String credentialType,
    @Schema(description = "Trust status", example = "TRUSTED") String trustStatus,
    @Schema(description = "Revocation status", example = "VALID") String revocationStatus,
    @Schema(description = "Assurance level", example = "HIGH") String assuranceLevel,
    @Schema(description = "Registration source", example = "ADMIN_BIND") String registrationSource,
    @Schema(description = "SHA-256 thumbprint of the certificate") String thumbprintSha256,
    @Schema(description = "Subject distinguished name") String subjectDn,
    @Schema(description = "Issuer distinguished name") String issuerDn,
    @Schema(description = "Certificate serial number") String serialNumber,
    @Schema(description = "Certificate valid-from timestamp (UTC)") Instant notBefore,
    @Schema(description = "Certificate valid-until timestamp (UTC)") Instant notAfter,
    @Schema(description = "Email or username of the approver") String approvedBy,
    @Schema(description = "Approval timestamp (UTC)") Instant approvedAt,
    @Schema(description = "Email or username of the user who revoked this credential",
        nullable = true) String revokedBy,
    @Schema(description = "Revocation timestamp (UTC)", nullable = true) Instant revokedAt,
    @Schema(description = "True if this is the user's primary credential") boolean primary,
    @Schema(description = "Record creation timestamp (UTC)") Instant createdAt,
    @Schema(description = "Record last-updated timestamp (UTC)") Instant updatedAt) {}
