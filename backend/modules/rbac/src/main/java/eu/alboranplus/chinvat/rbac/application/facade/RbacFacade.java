package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import java.util.List;
import java.util.Set;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;

public interface RbacFacade {
  RoleView getRole(String roleName);

  Set<String> resolvePermissions(Set<String> roleNames);

  List<PermissionView> listPermissions();

  PermissionView createPermission(String code, String description, String actor);
  PageResponse<PermissionView> listPermissionsPaged(PaginationRequest paginationRequest);

  PermissionView updatePermission(String code, String description, String actor);

  void deletePermission(String code, String actor);

  UserRolesView getUserRoles(Long userId);

  void assignRoleToUser(Long userId, String roleName, String assignedBy);

  void removeRoleFromUser(Long userId, String roleName, String actor);
}
