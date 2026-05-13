package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthPasswordResetTokenPort {
  void save(
      UUID userId,
      String rawCode,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent);

  Optional<UUID> consume(UUID userId, String rawCode, Instant now);
}

