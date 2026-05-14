package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemovePermissionFromRoleUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public RemovePermissionFromRoleUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  @Transactional
  public void execute(String roleName, String permissionCode) {
    if (!rbacRepositoryPort.roleExists(roleName)) {
      throw new RoleNotFoundException("Role not found: " + roleName);
    }
    rbacRepositoryPort.removePermissionFromRole(roleName, permissionCode);
  }
}
