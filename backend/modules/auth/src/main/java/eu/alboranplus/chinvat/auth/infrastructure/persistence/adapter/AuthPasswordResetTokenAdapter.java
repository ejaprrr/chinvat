package eu.alboranplus.chinvat.auth.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.infrastructure.persistence.entity.AuthPasswordResetJpaEntity;
import eu.alboranplus.chinvat.auth.infrastructure.persistence.jpa.AuthPasswordResetJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthPasswordResetTokenAdapter implements AuthPasswordResetTokenPort {

  private final AuthPasswordResetJpaRepository repository;

  public AuthPasswordResetTokenAdapter(AuthPasswordResetJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(
      Long userId,
      String rawCode,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent) {
    repository.revokeActiveByUserId(userId, issuedAt);

    String hash = sha256Hex(rawCode);
    AuthPasswordResetJpaEntity entity =
        new AuthPasswordResetJpaEntity(
            UUID.randomUUID(), userId, hash, issuedAt, expiresAt, clientIp, userAgent);
    repository.save(entity);
  }

  @Override
  @Transactional
  public Optional<Long> consume(Long userId, String rawCode, Instant now) {
    String hash = sha256Hex(rawCode);
    Optional<AuthPasswordResetJpaEntity> active =
        repository.findActiveByUserIdAndTokenHash(userId, hash, now);
    if (active.isEmpty()) {
      return Optional.empty();
    }
    int updated = repository.consumeByUserIdAndTokenHash(userId, hash, now);
    return updated > 0 ? active.map(AuthPasswordResetJpaEntity::getUserId) : Optional.empty();
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}

