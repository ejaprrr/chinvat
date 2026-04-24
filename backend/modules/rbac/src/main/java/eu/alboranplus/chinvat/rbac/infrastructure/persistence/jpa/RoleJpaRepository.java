package eu.alboranplus.chinvat.rbac.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity.RoleJpaEntity;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
  Optional<RoleJpaEntity> findByRoleNameIgnoreCase(String roleName);

  Set<RoleJpaEntity> findByRoleNameIn(Set<String> roleNames);
}
