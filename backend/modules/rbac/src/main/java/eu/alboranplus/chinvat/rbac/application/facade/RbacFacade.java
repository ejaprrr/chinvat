package eu.alboranplus.chinvat.rbac.application.facade;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import java.util.Set;

public interface RbacFacade {
  RoleView getRole(String roleName);

  Set<String> resolvePermissions(Set<String> roleNames);
}
