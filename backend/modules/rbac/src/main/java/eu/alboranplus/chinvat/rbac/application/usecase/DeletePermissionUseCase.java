package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeletePermissionUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public DeletePermissionUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public void execute(String code) {
    if (rbacRepositoryPort.findPermissionByCode(code).isEmpty()) {
      throw new PermissionNotFoundException("Permission not found: " + code);
    }
    rbacRepositoryPort.deletePermissionByCode(code);
  }
}
