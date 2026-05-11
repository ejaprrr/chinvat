package eu.alboranplus.chinvat.users.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
  boolean existsByEmailIgnoreCase(String email);

  boolean existsByUsernameIgnoreCase(String username);

  Optional<UserAccountJpaEntity> findByEmailIgnoreCase(String email);

  Optional<UserAccountJpaEntity> findByUsernameIgnoreCase(String username);
}

