package eu.alboranplus.chinvat.rbac.application.port.out;

import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.domain.model.PermissionDefinition;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RbacRepositoryPort {
  Optional<RoleDefinition> findByRoleName(String roleName);

  Set<RoleDefinition> findByRoleNames(Set<String> roleNames);

  List<PermissionDefinition> findAllPermissions();

  Optional<PermissionDefinition> findPermissionByCode(String permissionCode);

  PermissionDefinition createPermission(PermissionDefinition permissionDefinition);

  PermissionDefinition updatePermission(PermissionDefinition permissionDefinition);

  void deletePermissionByCode(String permissionCode);

  boolean roleExists(String roleName);

  boolean userExists(Long userId);

  void assignRoleToUser(Long userId, String roleName, String assignedBy);

  void removeRoleFromUser(Long userId, String roleName);

  Set<String> findRoleNamesByUserId(Long userId);
}
