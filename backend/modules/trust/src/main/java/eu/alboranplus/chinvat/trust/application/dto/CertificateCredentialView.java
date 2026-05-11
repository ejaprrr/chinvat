package eu.alboranplus.chinvat.trust.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CertificateCredentialView(
    UUID id,
    UUID userId,
    String providerCode,
    String credentialType,
    String trustStatus,
    String revocationStatus,
    String assuranceLevel,
    String registrationSource,
    String certificatePem,
    String thumbprintSha256,
    String subjectDn,
    String issuerDn,
    String serialNumber,
    Instant notBefore,
    Instant notAfter,
    String approvedBy,
    Instant approvedAt,
    String revokedBy,
    Instant revokedAt,
    boolean primary,
    Instant createdAt,
    Instant updatedAt) {}
