package eu.alboranplus.chinvat.auth.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.auth.infrastructure.persistence.entity.AuthPasswordResetJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthPasswordResetJpaRepository
    extends JpaRepository<AuthPasswordResetJpaEntity, UUID> {

  @Query(
      "SELECT r FROM AuthPasswordResetJpaEntity r "
          + "WHERE r.userId = :userId "
          + "AND r.resetTokenHash = :hash "
          + "AND r.consumedAt IS NULL "
          + "AND r.expiresAt > :now")
  Optional<AuthPasswordResetJpaEntity> findActiveByUserIdAndTokenHash(
      @Param("userId") Long userId,
      @Param("hash") String hash, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthPasswordResetJpaEntity r "
          + "SET r.consumedAt = :now "
          + "WHERE r.userId = :userId "
          + "AND r.resetTokenHash = :hash "
          + "AND r.consumedAt IS NULL "
          + "AND r.expiresAt > :now")
  int consumeByUserIdAndTokenHash(
      @Param("userId") Long userId,
      @Param("hash") String hash, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthPasswordResetJpaEntity r "
          + "SET r.consumedAt = :now "
          + "WHERE r.userId = :userId "
          + "AND r.consumedAt IS NULL "
          + "AND r.expiresAt > :now")
  int revokeActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}

