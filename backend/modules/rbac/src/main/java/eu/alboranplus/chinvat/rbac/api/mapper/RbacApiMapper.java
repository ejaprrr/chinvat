package eu.alboranplus.chinvat.rbac.api.mapper;

import eu.alboranplus.chinvat.rbac.api.dto.PermissionResponse;
import eu.alboranplus.chinvat.rbac.api.dto.RoleResponse;
import eu.alboranplus.chinvat.rbac.api.dto.UserRolesResponse;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import org.springframework.stereotype.Component;

@Component
public class RbacApiMapper {

  public RoleResponse toResponse(RoleView roleView) {
    return new RoleResponse(roleView.roleName(), roleView.permissions());
  }

  public PermissionResponse toResponse(PermissionView permissionView) {
    return new PermissionResponse(permissionView.code(), permissionView.description());
  }

  public UserRolesResponse toResponse(UserRolesView userRolesView) {
    return new UserRolesResponse(userRolesView.userId(), userRolesView.roles());
  }
}
