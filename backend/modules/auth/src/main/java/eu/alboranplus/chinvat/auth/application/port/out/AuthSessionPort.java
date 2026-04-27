package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface AuthSessionPort {

  /** Persists a hashed token as an active session. Raw token is hashed by the adapter. */
  void save(
      Long userId,
      String rawToken,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent);

  /**
   * Returns the userId if the given raw token resolves to an active (non-expired, non-revoked)
   * session at the given point in time.
   */
  Optional<Long> findActiveUserId(String rawToken, Instant now);

  /** Revokes a single session by its raw token. No-op if already revoked. */
  void revokeByRawToken(String rawToken, Instant now);

  /** Revokes all active sessions for a user (logout-all). */
  void revokeAllByUserId(Long userId, Instant now);
}
