package eu.alboranplus.chinvat.users.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserPasswordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPasswordJpaRepository extends JpaRepository<UserPasswordJpaEntity, Long> {}
