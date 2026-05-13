package eu.alboranplus.chinvat.profile.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfileCertificateResponse(
    UUID id,
    String providerCode,
    String trustStatus,
    String revocationStatus,
    String assuranceLevel,
    String thumbprintSha256,
    String subjectDn,
    String issuerDn,
    String serialNumber,
    Instant notBefore,
    Instant notAfter,
    boolean primary,
    Instant createdAt,
    Instant updatedAt) {}
