package eu.alboranplus.chinvat.rbac.application.port.out;

import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.domain.model.PermissionDefinition;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RbacRepositoryPort {
  Optional<RoleDefinition> findByRoleName(String roleName);

  Set<RoleDefinition> findByRoleNames(Set<String> roleNames);

  List<PermissionDefinition> findAllPermissions();

  Page<PermissionDefinition> findAllPermissions(Pageable pageable);

  Optional<PermissionDefinition> findPermissionByCode(String permissionCode);

  PermissionDefinition createPermission(PermissionDefinition permissionDefinition);

  PermissionDefinition updatePermission(PermissionDefinition permissionDefinition);

  void deletePermissionByCode(String permissionCode);

  boolean roleExists(String roleName);

  boolean userExists(UUID userId);

  void assignRoleToUser(UUID userId, String roleName, String assignedBy);

  void removeRoleFromUser(UUID userId, String roleName);

  Set<String> findRoleNamesByUserId(UUID userId);

  void assignPermissionToRole(String roleName, String permissionCode);

  void removePermissionFromRole(String roleName, String permissionCode);
}
