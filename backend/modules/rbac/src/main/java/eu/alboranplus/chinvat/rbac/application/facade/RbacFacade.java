package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import java.util.List;
import java.util.Set;

public interface RbacFacade {
  RoleView getRole(String roleName);

  Set<String> resolvePermissions(Set<String> roleNames);

  List<PermissionView> listPermissions();

  PermissionView createPermission(String code, String description);

  PermissionView updatePermission(String code, String description);

  void deletePermission(String code);

  UserRolesView getUserRoles(Long userId);

  void assignRoleToUser(Long userId, String roleName, String assignedBy);

  void removeRoleFromUser(Long userId, String roleName);
}
