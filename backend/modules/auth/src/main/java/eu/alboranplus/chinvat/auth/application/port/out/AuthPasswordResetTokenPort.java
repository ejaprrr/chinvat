package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface AuthPasswordResetTokenPort {
  void save(
      Long userId,
      String rawCode,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent);

  Optional<Long> consume(Long userId, String rawCode, Instant now);
}

