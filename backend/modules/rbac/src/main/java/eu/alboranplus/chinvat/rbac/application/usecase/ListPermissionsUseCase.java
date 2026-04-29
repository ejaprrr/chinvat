package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPermissionsUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public ListPermissionsUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public List<PermissionView> execute() {
    return rbacRepositoryPort.findAllPermissions().stream()
        .map(permission -> new PermissionView(permission.permissionCode(), permission.description()))
        .toList();
  }
}
