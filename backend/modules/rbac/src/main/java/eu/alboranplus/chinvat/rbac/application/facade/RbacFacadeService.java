package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.usecase.GetRoleUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ResolvePermissionsUseCase;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RbacFacadeService implements RbacFacade {

  private final GetRoleUseCase getRoleUseCase;
  private final ResolvePermissionsUseCase resolvePermissionsUseCase;

  public RbacFacadeService(
      GetRoleUseCase getRoleUseCase, ResolvePermissionsUseCase resolvePermissionsUseCase) {
    this.getRoleUseCase = getRoleUseCase;
    this.resolvePermissionsUseCase = resolvePermissionsUseCase;
  }

  @Override
  public RoleView getRole(String roleName) {
    return getRoleUseCase.execute(roleName);
  }

  @Override
  public Set<String> resolvePermissions(Set<String> roleNames) {
    return resolvePermissionsUseCase.execute(roleNames);
  }
}
