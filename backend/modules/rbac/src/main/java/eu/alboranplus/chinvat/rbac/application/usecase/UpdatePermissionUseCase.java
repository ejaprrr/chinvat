package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.model.PermissionDefinition;
import org.springframework.stereotype.Service;

@Service
public class UpdatePermissionUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public UpdatePermissionUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public PermissionView execute(String code, String description) {
    PermissionDefinition existing =
        rbacRepositoryPort
            .findPermissionByCode(code)
            .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + code));

    PermissionDefinition updated =
        rbacRepositoryPort.updatePermission(new PermissionDefinition(existing.permissionCode(), description));
    return new PermissionView(updated.permissionCode(), updated.description());
  }
}
