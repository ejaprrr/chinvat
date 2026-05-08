package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPermissionService authPermissionService;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private ValidateTokenUseCase sut;

  private final AuthUserProjection activeUser =
      new AuthUserProjection(1L, "alice@example.com", "Alice", Set.of("USER"), true);

  @Test
  void execute_validToken_returnsPopulatedPrincipal() {
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("valid-token", NOW)).willReturn(Optional.of(1L));
    given(authUsersPort.findById(1L)).willReturn(Optional.of(activeUser));
    given(authPermissionService.resolvePermissions(1L, Set.of("USER")))
      .willReturn(Set.of("PROFILE:READ"));

    Optional<TokenPrincipal> result = sut.execute("valid-token");

    assertThat(result).isPresent();
    TokenPrincipal principal = result.get();
    assertThat(principal.userId()).isEqualTo(1L);
    assertThat(principal.email()).isEqualTo("alice@example.com");
    assertThat(principal.roles()).containsExactly("USER");
    assertThat(principal.permissions()).containsExactly("PROFILE:READ");
  }

  @Test
  void execute_expiredOrRevokedToken_returnsEmpty() {
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("expired-token", NOW)).willReturn(Optional.empty());

    assertThat(sut.execute("expired-token")).isEmpty();
  }

  @Test
  void execute_tokenValidButUserInactive_returnsEmpty() {
    AuthUserProjection inactive =
        new AuthUserProjection(2L, "bob@example.com", "Bob", Set.of("USER"), false);
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("some-token", NOW)).willReturn(Optional.of(2L));
    given(authUsersPort.findById(2L)).willReturn(Optional.of(inactive));

    assertThat(sut.execute("some-token")).isEmpty();
  }

  @Test
  void execute_tokenValidButUserDeleted_returnsEmpty() {
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("orphan-token", NOW)).willReturn(Optional.of(99L));
    given(authUsersPort.findById(99L)).willReturn(Optional.empty());

    assertThat(sut.execute("orphan-token")).isEmpty();
    verify(authPermissionService, never()).resolvePermissions(99L, Set.of("USER"));
  }
}
