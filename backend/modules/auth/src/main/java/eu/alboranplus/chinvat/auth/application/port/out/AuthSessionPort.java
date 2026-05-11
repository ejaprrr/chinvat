package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthSessionPort {

  /** Persists a hashed token as an active session. Raw token is hashed by the adapter. */
  void save(
      Long userId,
      eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind tokenKind,
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

  /** Returns active sessions for a user (non-expired, not revoked), most recent first. */
  java.util.List<eu.alboranplus.chinvat.auth.application.dto.AuthSessionView> listActiveSessionsByUserId(
      Long userId, Instant now);

  /** Returns paginated active sessions for a user (non-expired, not revoked), most recent first. */
  Page<eu.alboranplus.chinvat.auth.application.dto.AuthSessionView> listActiveSessionsByUserIdPaged(
      Long userId, Instant now, Pageable pageable);

  /** Returns active session info by session id (used for ownership checks). */
  java.util.Optional<eu.alboranplus.chinvat.auth.application.dto.AuthSessionView> findActiveSessionById(
      java.util.UUID sessionId, Instant now);

  /** Revokes a single active session by its session id. No-op if already revoked/expired. */
  void revokeActiveSessionById(java.util.UUID sessionId, Instant now);
}
