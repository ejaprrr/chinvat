package eu.alboranplus.chinvat.trust.api.dto;

import java.time.Instant;

public record SyncTrustedProvidersResponse(
    String sourceUrl,
    int processedLotlCount,
    int processedTlCount,
    int trustedCertificates,
    Instant synchronizedAt) {}
