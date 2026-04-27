package eu.alboranplus.chinvat.auth.application.dto;

import java.time.Instant;

public record IssuedTokenPair(
    String accessToken, String refreshToken, Instant expiresAt, Instant refreshExpiresAt) {}

