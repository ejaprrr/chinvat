package eu.alboranplus.chinvat.auth.application.service;

import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthPermissionService {

  private final AuthRbacPort authRbacPort;
  private final PermissionCacheFacade permissionCacheFacade;

  public AuthPermissionService(
      AuthRbacPort authRbacPort, PermissionCacheFacade permissionCacheFacade) {
    this.authRbacPort = authRbacPort;
    this.permissionCacheFacade = permissionCacheFacade;
  }

  public Set<String> resolvePermissions(Long userId, Set<String> roles) {
    return permissionCacheFacade
        .findUserPermissions(userId)
        .orElseGet(
            () -> {
              Set<String> permissions = authRbacPort.resolvePermissions(roles);
              permissionCacheFacade.cacheUserPermissions(userId, permissions);
              return permissions;
            });
  }
}