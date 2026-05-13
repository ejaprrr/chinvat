package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.UserNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignRoleToUserUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public AssignRoleToUserUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  @Transactional
  public void execute(UUID userId, String roleName, String assignedBy) {
    if (!rbacRepositoryPort.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }
    if (!rbacRepositoryPort.roleExists(roleName)) {
      throw new RoleNotFoundException("Role not found: " + roleName);
    }

    rbacRepositoryPort.assignRoleToUser(userId, roleName, assignedBy);
  }
}
