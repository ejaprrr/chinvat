package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetRoleUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public GetRoleUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public RoleView execute(String roleName) {
    return rbacRepositoryPort
        .findByRoleName(roleName)
        .map(role -> new RoleView(role.roleName(), role.permissions()))
        .orElseGet(
            () -> {
              if (BuiltinRolePermissions.permissionsFor(roleName).isEmpty()) {
                throw new RoleNotFoundException("Role not found: " + roleName);
              }
              return new RoleView(
                  roleName.toUpperCase(), BuiltinRolePermissions.permissionsFor(roleName));
            });
  }
}
