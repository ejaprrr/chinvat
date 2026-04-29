package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class GetMeUseCaseTest {

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthRbacPort authRbacPort;

  @InjectMocks private GetMeUseCase sut;

  @Test
  void execute_returnsMeViewWithResolvedPermissions() {
    TokenPrincipal principal =
        new TokenPrincipal(1L, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));

    AuthUserProjection user =
        new AuthUserProjection(
            1L, "alice@example.com", "Alice", Set.of("USER"), true);

    given(authUsersPort.findById(1L)).willReturn(Optional.of(user));
    given(authRbacPort.resolvePermissions(user.roles())).willReturn(Set.of("PROFILE:READ"));

    AuthMeView me = sut.execute(principal);

    assertThat(me.id()).isEqualTo(1L);
    assertThat(me.email()).isEqualTo("alice@example.com");
    assertThat(me.displayName()).isEqualTo("Alice");
    assertThat(me.roles()).containsExactly("USER");
    assertThat(me.permissions()).containsExactly("PROFILE:READ");
  }
}

