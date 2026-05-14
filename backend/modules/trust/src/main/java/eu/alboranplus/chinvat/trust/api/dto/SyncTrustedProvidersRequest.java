package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TSL/LOTL synchronization request")
public record SyncTrustedProvidersRequest(
    @Schema(description = "When true, forces an online refresh of the trust lists; when false or null, uses cached data",
        nullable = true) Boolean onlineRefresh) {}
