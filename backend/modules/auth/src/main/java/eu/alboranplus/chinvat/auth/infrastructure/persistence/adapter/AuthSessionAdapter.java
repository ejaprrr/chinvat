package eu.alboranplus.chinvat.auth.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;
import eu.alboranplus.chinvat.auth.infrastructure.persistence.repository.AuthSessionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;

@Component
public class AuthSessionAdapter implements AuthSessionPort {

  private final AuthSessionJpaRepository repository;

  public AuthSessionAdapter(AuthSessionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(
      Long userId,
      AuthSessionTokenKind tokenKind,
      String rawToken,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent) {
    String hash = sha256Hex(rawToken);
    AuthSessionJpaEntity entity =
        new AuthSessionJpaEntity(
            UUID.randomUUID(), userId, hash, tokenKind, issuedAt, expiresAt, clientIp, userAgent);
    repository.save(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Long> findActiveUserId(String rawToken, Instant now) {
    String hash = sha256Hex(rawToken);
    return repository
        .findBySessionTokenHash(hash)
        .filter(s -> s.getRevokedAt() == null)
        .filter(s -> s.getExpiresAt().isAfter(now))
        .map(AuthSessionJpaEntity::getUserId);
  }

  @Override
  @Transactional
  public void revokeByRawToken(String rawToken, Instant now) {
    String hash = sha256Hex(rawToken);
    repository.revokeByTokenHash(hash, now);
  }

  @Override
  @Transactional
  public void revokeAllByUserId(Long userId, Instant now) {
    repository.revokeAllByUserId(userId, now);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuthSessionView> listActiveSessionsByUserId(Long userId, Instant now) {
    return repository
        .findActiveByUserIdOrderByIssuedAtDesc(userId, now)
        .stream()
        .map(this::toView)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AuthSessionView> findActiveSessionById(UUID sessionId, Instant now) {
    return repository
        .findActiveById(sessionId, now)
        .map(this::toView);
  }

  @Override
  @Transactional
  public void revokeActiveSessionById(UUID sessionId, Instant now) {
    repository.revokeActiveById(sessionId, now);
  }

  private AuthSessionView toView(AuthSessionJpaEntity entity) {
    return new AuthSessionView(
        entity.getId(),
        entity.getUserId(),
        entity.getSessionTokenKind(),
        entity.getIssuedAt(),
        entity.getExpiresAt(),
        entity.getClientIp(),
        entity.getUserAgent());
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
