package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignPermissionToRoleUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public AssignPermissionToRoleUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  @Transactional
  public void execute(String roleName, String permissionCode) {
    if (!rbacRepositoryPort.roleExists(roleName)) {
      throw new RoleNotFoundException("Role not found: " + roleName);
    }
    if (rbacRepositoryPort.findPermissionByCode(permissionCode).isEmpty()) {
      throw new PermissionNotFoundException("Permission not found: " + permissionCode);
    }
    rbacRepositoryPort.assignPermissionToRole(roleName, permissionCode);
  }
}
