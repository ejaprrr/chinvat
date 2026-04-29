package eu.alboranplus.chinvat.auth.application.dto;

import java.time.Instant;

public record PasswordResetRequestResult(
    String resetToken, Instant requestedAt) {}

