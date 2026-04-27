package eu.alboranplus.chinvat.rbac.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolvePermissionsUseCaseTest {

  @Mock private RbacRepositoryPort rbacRepositoryPort;

  @InjectMocks private ResolvePermissionsUseCase sut;

  @Test
  void execute_builtinRoleOnly_returnsBuiltinPermissions() {
    given(rbacRepositoryPort.findByRoleNames(Set.of("USER"))).willReturn(Set.of());

    Set<String> perms = sut.execute(Set.of("USER"));

    assertThat(perms).containsExactly("PROFILE:READ");
  }

  @Test
  void execute_dbRoleWithExtraPermission_mergesWithBuiltin() {
    RoleDefinition dbRole = new RoleDefinition("USER", Set.of("CUSTOM:PERM"));
    given(rbacRepositoryPort.findByRoleNames(Set.of("USER"))).willReturn(Set.of(dbRole));

    Set<String> perms = sut.execute(Set.of("USER"));

    assertThat(perms).containsExactlyInAnyOrder("PROFILE:READ", "CUSTOM:PERM");
  }

  @Test
  void execute_multipleRoles_unionsAllPermissions() {
    given(rbacRepositoryPort.findByRoleNames(Set.of("USER", "ADMIN"))).willReturn(Set.of());

    Set<String> perms = sut.execute(Set.of("USER", "ADMIN"));

    assertThat(perms)
        .containsExactlyInAnyOrder("PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE");
  }

  @Test
  void execute_emptyRoles_returnsEmpty() {
    given(rbacRepositoryPort.findByRoleNames(Set.of())).willReturn(Set.of());

    assertThat(sut.execute(Set.of())).isEmpty();
  }

  @Test
  void execute_unknownRole_returnsEmpty() {
    given(rbacRepositoryPort.findByRoleNames(Set.of("GHOST"))).willReturn(Set.of());

    assertThat(sut.execute(Set.of("GHOST"))).isEmpty();
  }

  @Test
  void execute_normalizesRolesToUpperCase() {
    given(rbacRepositoryPort.findByRoleNames(Set.of("USER"))).willReturn(Set.of());

    Set<String> perms = sut.execute(Set.of("user"));

    assertThat(perms).containsExactly("PROFILE:READ");
  }

  @Test
  void execute_superadmin_hasAllPermissions() {
    given(rbacRepositoryPort.findByRoleNames(Set.of("SUPERADMIN"))).willReturn(Set.of());

    Set<String> perms = sut.execute(Set.of("SUPERADMIN"));

    assertThat(perms)
        .containsExactlyInAnyOrder(
            "PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE", "RBAC:MANAGE", "AUTH:MANAGE");
  }
}
