package eu.alboranplus.chinvat.eidas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "eIDAS identity provider definition")
public record EidasProviderResponse(
    @Schema(description = "Unique provider code", example = "EIDAS_EU") String code,
    @Schema(description = "Human-readable provider name", example = "EU eIDAS Node") String displayName,
    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "ES") String countryCode,
    @Schema(description = "Whether this provider is active and available for use") boolean enabled) {}
