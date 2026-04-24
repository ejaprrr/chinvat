package eu.alboranplus.chinvat.rbac.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.jpa.RoleJpaRepository;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.mapper.RoleJpaMapper;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RbacRepositoryAdapter implements RbacRepositoryPort {

  private final RoleJpaRepository roleJpaRepository;
  private final RoleJpaMapper roleJpaMapper;

  public RbacRepositoryAdapter(RoleJpaRepository roleJpaRepository, RoleJpaMapper roleJpaMapper) {
    this.roleJpaRepository = roleJpaRepository;
    this.roleJpaMapper = roleJpaMapper;
  }

  @Override
  public Optional<RoleDefinition> findByRoleName(String roleName) {
    return roleJpaRepository.findByRoleNameIgnoreCase(roleName).map(roleJpaMapper::toDomain);
  }

  @Override
  public Set<RoleDefinition> findByRoleNames(Set<String> roleNames) {
    return roleJpaRepository.findByRoleNameIn(roleNames).stream()
        .map(roleJpaMapper::toDomain)
        .collect(Collectors.toUnmodifiableSet());
  }
}
