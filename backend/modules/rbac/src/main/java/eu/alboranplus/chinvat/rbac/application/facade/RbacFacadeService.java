package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import eu.alboranplus.chinvat.rbac.application.usecase.AssignRoleToUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.CreatePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.DeletePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetUserRolesUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetRoleUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ListPermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.RemoveRoleFromUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ResolvePermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.UpdatePermissionUseCase;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RbacFacadeService implements RbacFacade {

  private final GetRoleUseCase getRoleUseCase;
  private final ResolvePermissionsUseCase resolvePermissionsUseCase;
  private final ListPermissionsUseCase listPermissionsUseCase;
  private final CreatePermissionUseCase createPermissionUseCase;
  private final UpdatePermissionUseCase updatePermissionUseCase;
  private final DeletePermissionUseCase deletePermissionUseCase;
  private final GetUserRolesUseCase getUserRolesUseCase;
  private final AssignRoleToUserUseCase assignRoleToUserUseCase;
  private final RemoveRoleFromUserUseCase removeRoleFromUserUseCase;

  public RbacFacadeService(
      GetRoleUseCase getRoleUseCase,
      ResolvePermissionsUseCase resolvePermissionsUseCase,
      ListPermissionsUseCase listPermissionsUseCase,
      CreatePermissionUseCase createPermissionUseCase,
      UpdatePermissionUseCase updatePermissionUseCase,
      DeletePermissionUseCase deletePermissionUseCase,
      GetUserRolesUseCase getUserRolesUseCase,
      AssignRoleToUserUseCase assignRoleToUserUseCase,
      RemoveRoleFromUserUseCase removeRoleFromUserUseCase) {
    this.getRoleUseCase = getRoleUseCase;
    this.resolvePermissionsUseCase = resolvePermissionsUseCase;
    this.listPermissionsUseCase = listPermissionsUseCase;
    this.createPermissionUseCase = createPermissionUseCase;
    this.updatePermissionUseCase = updatePermissionUseCase;
    this.deletePermissionUseCase = deletePermissionUseCase;
    this.getUserRolesUseCase = getUserRolesUseCase;
    this.assignRoleToUserUseCase = assignRoleToUserUseCase;
    this.removeRoleFromUserUseCase = removeRoleFromUserUseCase;
  }

  @Override
  public RoleView getRole(String roleName) {
    return getRoleUseCase.execute(roleName);
  }

  @Override
  public Set<String> resolvePermissions(Set<String> roleNames) {
    return resolvePermissionsUseCase.execute(roleNames);
  }

  @Override
  public List<PermissionView> listPermissions() {
    return listPermissionsUseCase.execute();
  }

  @Override
  public PermissionView createPermission(String code, String description) {
    return createPermissionUseCase.execute(code, description);
  }

  @Override
  public PermissionView updatePermission(String code, String description) {
    return updatePermissionUseCase.execute(code, description);
  }

  @Override
  public void deletePermission(String code) {
    deletePermissionUseCase.execute(code);
  }

  @Override
  public UserRolesView getUserRoles(Long userId) {
    return getUserRolesUseCase.execute(userId);
  }

  @Override
  public void assignRoleToUser(Long userId, String roleName, String assignedBy) {
    assignRoleToUserUseCase.execute(userId, roleName, assignedBy);
  }

  @Override
  public void removeRoleFromUser(Long userId, String roleName) {
    removeRoleFromUserUseCase.execute(userId, roleName);
  }
}
