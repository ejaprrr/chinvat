package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.dto.UserRolesView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetUserRolesUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public GetUserRolesUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public UserRolesView execute(Long userId) {
    if (!rbacRepositoryPort.userExists(userId)) {
      throw new UserNotFoundException("User not found: " + userId);
    }

    return new UserRolesView(userId, rbacRepositoryPort.findRoleNamesByUserId(userId));
  }
}
