package eu.alboranplus.chinvat.auth.infrastructure.persistence.repository;

import eu.alboranplus.chinvat.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionJpaEntity, UUID> {

  Optional<AuthSessionJpaEntity> findBySessionTokenHash(String sessionTokenHash);

  @Query(
      "SELECT s FROM AuthSessionJpaEntity s "
          + "WHERE s.userId = :userId "
          + "AND s.revokedAt IS NULL "
          + "AND s.expiresAt > :now "
          + "ORDER BY s.issuedAt DESC")
  List<AuthSessionJpaEntity> findActiveByUserIdOrderByIssuedAtDesc(
      @Param("userId") Long userId, @Param("now") Instant now);

  @Query(
      "SELECT s FROM AuthSessionJpaEntity s "
          + "WHERE s.id = :sessionId "
          + "AND s.revokedAt IS NULL "
          + "AND s.expiresAt > :now")
  Optional<AuthSessionJpaEntity> findActiveById(
      @Param("sessionId") UUID sessionId, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthSessionJpaEntity s SET s.revokedAt = :now "
          + "WHERE s.sessionTokenHash = :hash AND s.revokedAt IS NULL")
  void revokeByTokenHash(@Param("hash") String hash, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthSessionJpaEntity s SET s.revokedAt = :now "
          + "WHERE s.userId = :userId AND s.revokedAt IS NULL")
  void revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthSessionJpaEntity s SET s.revokedAt = :now "
          + "WHERE s.id = :sessionId AND s.revokedAt IS NULL")
  void revokeActiveById(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthSessionJpaEntity s SET s.revokedAt = :now "
          + "WHERE s.userId = :userId "
          + "AND s.sessionTokenKind = :tokenKind "
          + "AND s.revokedAt IS NULL")
  void revokeActiveByUserIdAndTokenKind(
      @Param("userId") Long userId,
      @Param("tokenKind") AuthSessionTokenKind tokenKind,
      @Param("now") Instant now);
}
