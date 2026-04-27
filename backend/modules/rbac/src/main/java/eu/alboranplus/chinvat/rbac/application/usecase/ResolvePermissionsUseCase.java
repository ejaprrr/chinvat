package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class ResolvePermissionsUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public ResolvePermissionsUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public Set<String> execute(Set<String> roleNames) {
    Set<String> normalizedRoles =
        roleNames.stream()
            .map(String::trim)
            .map(String::toUpperCase)
            .collect(Collectors.toUnmodifiableSet());

    Set<String> repositoryPermissions =
        rbacRepositoryPort.findByRoleNames(normalizedRoles).stream()
            .flatMap(role -> role.permissions().stream())
            .collect(Collectors.toUnmodifiableSet());

    Set<String> builtinPermissions =
        normalizedRoles.stream()
            .flatMap(roleName -> BuiltinRolePermissions.permissionsFor(roleName).stream())
            .collect(Collectors.toUnmodifiableSet());

    return Stream.concat(repositoryPermissions.stream(), builtinPermissions.stream())
        .collect(Collectors.toUnmodifiableSet());
  }
}
