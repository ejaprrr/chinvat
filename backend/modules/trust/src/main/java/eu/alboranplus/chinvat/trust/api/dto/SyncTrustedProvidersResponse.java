package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "TSL/LOTL synchronization result")
public record SyncTrustedProvidersResponse(
    @Schema(description = "Source URL of the LOTL used for synchronization") String sourceUrl,
    @Schema(description = "Number of LOTL records processed") int processedLotlCount,
    @Schema(description = "Number of Trust Lists processed") int processedTlCount,
    @Schema(description = "Total number of trusted certificates loaded") int trustedCertificates,
    @Schema(description = "Timestamp when synchronization was completed (UTC)") Instant synchronizedAt) {}
