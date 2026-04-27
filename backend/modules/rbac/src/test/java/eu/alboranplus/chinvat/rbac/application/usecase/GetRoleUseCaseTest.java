package eu.alboranplus.chinvat.rbac.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRoleUseCaseTest {

  @Mock private RbacRepositoryPort rbacRepositoryPort;

  @InjectMocks private GetRoleUseCase sut;

  @Test
  void execute_roleFoundInDb_returnsDbView() {
    RoleDefinition dbRole = new RoleDefinition("ADMIN", Set.of("CUSTOM:PERM"));
    given(rbacRepositoryPort.findByRoleName("ADMIN")).willReturn(Optional.of(dbRole));

    RoleView result = sut.execute("ADMIN");

    assertThat(result.roleName()).isEqualTo("ADMIN");
    assertThat(result.permissions()).containsExactly("CUSTOM:PERM");
  }

  @Test
  void execute_notInDbButBuiltin_returnsBuiltinView() {
    given(rbacRepositoryPort.findByRoleName("USER")).willReturn(Optional.empty());

    RoleView result = sut.execute("USER");

    assertThat(result.roleName()).isEqualTo("USER");
    assertThat(result.permissions()).containsExactly("PROFILE:READ");
  }

  @Test
  void execute_notFoundAnywhere_throwsRoleNotFoundException() {
    given(rbacRepositoryPort.findByRoleName("UNKNOWN")).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute("UNKNOWN"))
        .isInstanceOf(RoleNotFoundException.class)
        .hasMessageContaining("UNKNOWN");
  }
}
