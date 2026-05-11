package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import eu.alboranplus.chinvat.rbac.application.usecase.AssignRoleToUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.CreatePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.DeletePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetUserRolesUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetRoleUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ListPermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ListPermissionsPagedUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.RemoveRoleFromUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ResolvePermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.UpdatePermissionUseCase;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RbacFacadeService implements RbacFacade {

  private final GetRoleUseCase getRoleUseCase;
  private final ResolvePermissionsUseCase resolvePermissionsUseCase;
  private final ListPermissionsUseCase listPermissionsUseCase;
  private final ListPermissionsPagedUseCase listPermissionsPagedUseCase;
  private final CreatePermissionUseCase createPermissionUseCase;
  private final UpdatePermissionUseCase updatePermissionUseCase;
  private final DeletePermissionUseCase deletePermissionUseCase;
  private final GetUserRolesUseCase getUserRolesUseCase;
  private final AssignRoleToUserUseCase assignRoleToUserUseCase;
  private final RemoveRoleFromUserUseCase removeRoleFromUserUseCase;
  private final AuditFacade auditFacade;
  private final PermissionCacheFacade permissionCacheFacade;

  public RbacFacadeService(
      GetRoleUseCase getRoleUseCase,
      ResolvePermissionsUseCase resolvePermissionsUseCase,
      ListPermissionsUseCase listPermissionsUseCase,
      ListPermissionsPagedUseCase listPermissionsPagedUseCase,
      CreatePermissionUseCase createPermissionUseCase,
      UpdatePermissionUseCase updatePermissionUseCase,
      DeletePermissionUseCase deletePermissionUseCase,
      GetUserRolesUseCase getUserRolesUseCase,
      AssignRoleToUserUseCase assignRoleToUserUseCase,
      RemoveRoleFromUserUseCase removeRoleFromUserUseCase,
      AuditFacade auditFacade,
      PermissionCacheFacade permissionCacheFacade) {
    this.getRoleUseCase = getRoleUseCase;
    this.resolvePermissionsUseCase = resolvePermissionsUseCase;
    this.listPermissionsUseCase = listPermissionsUseCase;
    this.listPermissionsPagedUseCase = listPermissionsPagedUseCase;
    this.createPermissionUseCase = createPermissionUseCase;
    this.updatePermissionUseCase = updatePermissionUseCase;
    this.deletePermissionUseCase = deletePermissionUseCase;
    this.getUserRolesUseCase = getUserRolesUseCase;
    this.assignRoleToUserUseCase = assignRoleToUserUseCase;
    this.removeRoleFromUserUseCase = removeRoleFromUserUseCase;
    this.auditFacade = auditFacade;
    this.permissionCacheFacade = permissionCacheFacade;
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
  public PermissionView createPermission(String code, String description, String actor) {
    PermissionView created = createPermissionUseCase.execute(code, description);
    permissionCacheFacade.evictAllUserPermissions();
    auditFacade.log(
        "RBAC_PERMISSION_CREATED",
        actor,
        null,
        AuditDetails.builder()
            .add("permissionCode", created.code())
            .add("description", created.description())
            .build());
    return created;
  }

  @Override
  public PermissionView updatePermission(String code, String description, String actor) {
    PermissionView updated = updatePermissionUseCase.execute(code, description);
    permissionCacheFacade.evictAllUserPermissions();
    auditFacade.log(
        "RBAC_PERMISSION_UPDATED",
        actor,
        null,
        AuditDetails.builder()
            .add("permissionCode", updated.code())
            .add("description", updated.description())
            .build());
    return updated;
  }

  @Override
  public void deletePermission(String code, String actor) {
    deletePermissionUseCase.execute(code);
    permissionCacheFacade.evictAllUserPermissions();
    auditFacade.log(
        "RBAC_PERMISSION_DELETED",
        actor,
        null,
        AuditDetails.builder().add("permissionCode", code).build());
  }

  @Override
  public UserRolesView getUserRoles(Long userId) {
    return getUserRolesUseCase.execute(userId);
  }

  @Override
  public void assignRoleToUser(Long userId, String roleName, String assignedBy) {
    assignRoleToUserUseCase.execute(userId, roleName, assignedBy);
    permissionCacheFacade.evictUserPermissions(userId);
    auditFacade.log(
        "RBAC_ROLE_ASSIGNED_TO_USER",
        assignedBy,
        null,
        AuditDetails.builder()
            .add("userId", userId)
            .add("roleName", roleName)
            .add("assignedBy", assignedBy)
            .build());
  }

  @Override
  public void removeRoleFromUser(Long userId, String roleName, String actor) {
    removeRoleFromUserUseCase.execute(userId, roleName);
    permissionCacheFacade.evictUserPermissions(userId);
    auditFacade.log(
        "RBAC_ROLE_REMOVED_FROM_USER",
        actor,
        null,
        AuditDetails.builder().add("userId", userId).add("roleName", roleName).build());
  }
  
  @Override
  public PageResponse<PermissionView> listPermissionsPaged(PaginationRequest paginationRequest) {
    return listPermissionsPagedUseCase.execute(paginationRequest);
  }
}
