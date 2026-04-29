package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface AuthPasswordResetTokenPort {
  void save(
      Long userId,
      String rawToken,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent);

  /**
   * Consumes a raw reset token and returns the associated user id if the token is valid
   * (not expired, not consumed).
   */
  Optional<Long> consume(String rawToken, Instant now);
}

