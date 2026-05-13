package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final Instant ACCESS_EXP = NOW.plusSeconds(900);
  private static final Instant REFRESH_EXP = NOW.plusSeconds(1_209_600);

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPermissionService authPermissionService;
  @Mock private AuthTokenIssuerPort authTokenIssuerPort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private LoginUseCase sut;

  private static final UUID UUID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID UUID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private final AuthUserProjection activeUser =
      new AuthUserProjection(UUID_1, "alice@example.com", "Alice", Set.of("USER"), true);

  private final IssuedTokenPair tokens =
      new IssuedTokenPair("access-token", "refresh-token", ACCESS_EXP, REFRESH_EXP);

  @Test
  void execute_validCredentials_returnsAuthResult() {
    LoginCommand cmd = new LoginCommand("alice@example.com", "secret", "127.0.0.1", "TestAgent");
    given(authUsersPort.findByEmail("alice@example.com")).willReturn(Optional.of(activeUser));
    given(authUsersPort.verifyPassword("alice@example.com", "secret")).willReturn(true);
    given(authPermissionService.resolvePermissions(UUID_1, Set.of("USER")))
      .willReturn(Set.of("PROFILE:READ"));
    given(authClockPort.now()).willReturn(NOW);
    given(authTokenIssuerPort.issue(UUID_1, "alice@example.com", NOW)).willReturn(tokens);

    AuthResult result = sut.execute(cmd);

    assertThat(result.userId()).isEqualTo(UUID_1);
    assertThat(result.email()).isEqualTo("alice@example.com");
    assertThat(result.permissions()).containsExactly("PROFILE:READ");
    assertThat(result.tokens().accessToken()).isEqualTo("access-token");
    assertThat(result.tokens().refreshToken()).isEqualTo("refresh-token");
  }

  @Test
  void execute_bothSessionsSaved() {
    LoginCommand cmd = new LoginCommand("alice@example.com", "secret", "127.0.0.1", "TestAgent");
    given(authUsersPort.findByEmail("alice@example.com")).willReturn(Optional.of(activeUser));
    given(authUsersPort.verifyPassword("alice@example.com", "secret")).willReturn(true);
    given(authPermissionService.resolvePermissions(UUID_1, Set.of("USER"))).willReturn(Set.of());
    given(authClockPort.now()).willReturn(NOW);
    given(authTokenIssuerPort.issue(UUID_1, "alice@example.com", NOW)).willReturn(tokens);

    sut.execute(cmd);

    // Access session
    verify(authSessionPort)
        .save(
            eq(UUID_1),
            eq(AuthSessionTokenKind.ACCESS),
            eq("access-token"),
            eq(NOW),
            eq(ACCESS_EXP),
            eq("127.0.0.1"),
            eq("TestAgent"));
    // Refresh session
    verify(authSessionPort)
        .save(
            eq(UUID_1),
            eq(AuthSessionTokenKind.REFRESH),
            eq("refresh-token"),
            eq(NOW),
            eq(REFRESH_EXP),
            eq("127.0.0.1"),
            eq("TestAgent"));
    verify(authSessionPort, times(2)).save(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void execute_wrongPassword_throwsInvalidAuthentication() {
    LoginCommand cmd = new LoginCommand("alice@example.com", "wrong", "127.0.0.1", "Agent");
    given(authUsersPort.findByEmail("alice@example.com")).willReturn(Optional.of(activeUser));
    given(authUsersPort.verifyPassword("alice@example.com", "wrong")).willReturn(false);

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class)
        .hasMessageContaining("Invalid email or password");

    verify(authSessionPort, never()).save(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void execute_inactiveUser_throwsInvalidAuthentication() {
    AuthUserProjection inactive =
        new AuthUserProjection(UUID_2, "inactive@example.com", "Inactive", Set.of("USER"), false);
    LoginCommand cmd = new LoginCommand("inactive@example.com", "secret", "127.0.0.1", "Agent");
    given(authUsersPort.findByEmail("inactive@example.com")).willReturn(Optional.of(inactive));

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class);

    verify(authSessionPort, never()).save(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void execute_userNotFound_throwsInvalidAuthentication() {
    LoginCommand cmd = new LoginCommand("ghost@example.com", "secret", "127.0.0.1", "Agent");
    given(authUsersPort.findByEmail("ghost@example.com")).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class);
  }
}
