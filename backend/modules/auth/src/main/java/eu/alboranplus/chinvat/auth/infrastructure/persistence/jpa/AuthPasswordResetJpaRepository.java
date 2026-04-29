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
          + "WHERE r.resetTokenHash = :hash "
          + "AND r.consumedAt IS NULL "
          + "AND r.expiresAt > :now")
  Optional<AuthPasswordResetJpaEntity> findActiveByTokenHash(
      @Param("hash") String hash, @Param("now") Instant now);

  @Modifying
  @Query(
      "UPDATE AuthPasswordResetJpaEntity r "
          + "SET r.consumedAt = :now "
          + "WHERE r.resetTokenHash = :hash "
          + "AND r.consumedAt IS NULL "
          + "AND r.expiresAt > :now")
  int consumeByTokenHash(
      @Param("hash") String hash, @Param("now") Instant now);
}

