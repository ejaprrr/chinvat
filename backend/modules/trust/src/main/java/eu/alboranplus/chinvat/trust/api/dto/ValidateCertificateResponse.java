package eu.alboranplus.chinvat.trust.api.dto;

import java.time.Instant;
import java.util.List;

public record ValidateCertificateResponse(
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
