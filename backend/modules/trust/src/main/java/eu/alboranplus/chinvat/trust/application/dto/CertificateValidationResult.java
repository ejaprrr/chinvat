package eu.alboranplus.chinvat.trust.application.dto;

import java.time.Instant;
import java.util.List;

public record CertificateValidationResult(
    String thumbprintSha256,
    String subjectDn,
    String issuerDn,
    String serialNumber,
    Instant notBefore,
    Instant notAfter,
    boolean validNow,
    boolean trustedIssuer,
    String trustSource,
    List<String> keyUsageFlags,
    Instant validatedAt) {}
