package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;

public record EidasLoginView(
    String providerCode,
    String state,
    String authorizationUrl,
    Instant expiresAt) {}
