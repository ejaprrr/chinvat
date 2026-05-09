package eu.alboranplus.chinvat.trust.application.dto;

import java.time.Instant;

public record TrustedProviderSyncResult(
    String sourceUrl,
    int processedLotlCount,
    int processedTlCount,
    int trustedCertificates,
    Instant synchronizedAt) {}
