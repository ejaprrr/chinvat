package eu.alboranplus.chinvat.auth.api.dto;

import java.time.Instant;

public record AuthApiErrorResponse(String message, Instant timestamp) {}
