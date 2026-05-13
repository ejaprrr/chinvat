package eu.alboranplus.chinvat.auth.application.dto;

import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.UUID;

/**
 * Safe projection for session management endpoints (no raw tokens).
 */
public record AuthSessionView(
    UUID sessionId,
    UUID userId,
    AuthSessionTokenKind tokenKind,
    Instant issuedAt,
    Instant expiresAt,
    String clientIp,
    String userAgent) {}

