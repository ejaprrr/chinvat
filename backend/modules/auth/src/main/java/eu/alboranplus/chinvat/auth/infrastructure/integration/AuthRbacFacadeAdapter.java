package eu.alboranplus.chinvat.auth.infrastructure.integration;

import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AuthRbacFacadeAdapter implements AuthRbacPort {

  private final RbacFacade rbacFacade;

  public AuthRbacFacadeAdapter(RbacFacade rbacFacade) {
    this.rbacFacade = rbacFacade;
  }

  @Override
  public Set<String> resolvePermissions(Set<String> roleNames) {
    return rbacFacade.resolvePermissions(roleNames);
  }
}
