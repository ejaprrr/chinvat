package eu.alboranplus.chinvat.eidas.api.dto;

import java.time.Instant;

public record EidasLoginResponse(
    String providerCode,
    String state,
    String authorizationUrl,
    Instant expiresAt) {}
