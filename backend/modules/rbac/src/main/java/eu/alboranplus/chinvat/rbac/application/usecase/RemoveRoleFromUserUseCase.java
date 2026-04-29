package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveRoleFromUserUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public RemoveRoleFromUserUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  @Transactional
  public void execute(Long userId, String roleName) {
    if (!rbacRepositoryPort.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }
    if (!rbacRepositoryPort.roleExists(roleName)) {
      throw new RoleNotFoundException("Role not found: " + roleName);
    }

    rbacRepositoryPort.removeRoleFromUser(userId, roleName);
  }
}
