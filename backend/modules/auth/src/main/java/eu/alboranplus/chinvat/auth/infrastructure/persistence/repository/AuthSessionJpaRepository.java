package eu.alboranplus.chinvat.auth.infrastructure.persistence.repository;

import eu.alboranplus.chinvat.auth.infrastructure.persistence.entity.AuthSessionJpaEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionJpaEntity, UUID> {

  Optional<AuthSessionJpaEntity> findBySessionTokenHash(String sessionTokenHash);

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
}
