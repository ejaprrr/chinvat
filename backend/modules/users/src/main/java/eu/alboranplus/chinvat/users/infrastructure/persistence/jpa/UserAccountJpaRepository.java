package eu.alboranplus.chinvat.users.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, Long> {
  boolean existsByEmailIgnoreCase(String email);

  Optional<UserAccountJpaEntity> findByEmailIgnoreCase(String email);
}
