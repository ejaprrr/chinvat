package eu.alboranplus.chinvat.trust.api.dto;

import java.time.Instant;

public record TrustApiErrorResponse(String message, Instant timestamp) {}
