package eu.alboranplus.chinvat.eidas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "eIDAS login initiation response")
public record EidasLoginResponse(
    @Schema(description = "Provider code", example = "EIDAS_EU") String providerCode,
    @Schema(description = "Opaque state token used for CSRF validation", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") String state,
    @Schema(description = "URL to redirect the user to for eIDAS authentication") String authorizationUrl,
    @Schema(description = "Timestamp when the state token expires (UTC)") Instant expiresAt) {}
