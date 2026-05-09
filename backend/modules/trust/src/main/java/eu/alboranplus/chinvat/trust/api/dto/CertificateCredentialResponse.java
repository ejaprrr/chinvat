package eu.alboranplus.chinvat.trust.api.dto;

import java.time.Instant;

public record CertificateCredentialResponse(
    Long id,
    Long userId,
    String providerCode,
    String credentialType,
    String trustStatus,
    String revocationStatus,
    String assuranceLevel,
    String registrationSource,
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
