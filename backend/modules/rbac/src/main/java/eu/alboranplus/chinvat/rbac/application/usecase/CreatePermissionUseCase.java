package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionAlreadyExistsException;
import eu.alboranplus.chinvat.rbac.domain.model.PermissionDefinition;
import org.springframework.stereotype.Service;

@Service
public class CreatePermissionUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public CreatePermissionUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public PermissionView execute(String code, String description) {
    if (rbacRepositoryPort.findPermissionByCode(code).isPresent()) {
      throw new PermissionAlreadyExistsException("Permission already exists: " + code);
    }

    PermissionDefinition created =
        rbacRepositoryPort.createPermission(new PermissionDefinition(code, description));
    return new PermissionView(created.permissionCode(), created.description());
  }
}
