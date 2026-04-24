package eu.alboranplus.chinvat.rbac.application.port.out;

import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import java.util.Optional;
import java.util.Set;

public interface RbacRepositoryPort {
  Optional<RoleDefinition> findByRoleName(String roleName);

  Set<RoleDefinition> findByRoleNames(Set<String> roleNames);
}
