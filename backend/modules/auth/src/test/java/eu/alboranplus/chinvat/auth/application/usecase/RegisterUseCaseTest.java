package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUserRegistrationPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthUserRegistrationPort userRegistrationPort;
  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthRbacPort authRbacPort;
  @Mock private AuthTokenIssuerPort authTokenIssuerPort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private RegisterUseCase sut;

  @Test
  void execute_registersUserIssuesTokensAndSavesSessions() {
    RegisterCommand command =
        new RegisterCommand(
            "alice",
            "Alice",
            null,
            "alice@example.com",
            "secret",
            UserType.INDIVIDUAL,
            null,
            null,
            null,
            null,
            "en",
            "127.0.0.1",
            "TestAgent");

    AuthUserProjection user =
        new AuthUserProjection(1L, "alice@example.com", "Alice", Set.of("USER"), true);

    IssuedTokenPair tokens =
        new IssuedTokenPair(
            "access-token",
            "refresh-token",
            NOW.plusSeconds(900),
            NOW.plusSeconds(1_209_600));

    given(userRegistrationPort.register(command)).willReturn(1L);
    given(authUsersPort.findById(1L)).willReturn(Optional.of(user));
    given(authRbacPort.resolvePermissions(user.roles())).willReturn(Set.of("PROFILE:READ"));
    given(authClockPort.now()).willReturn(NOW);
    given(authTokenIssuerPort.issue(1L, "alice@example.com", NOW)).willReturn(tokens);

    AuthResult result = sut.execute(command);

    assertThat(result.userId()).isEqualTo(1L);
    assertThat(result.email()).isEqualTo("alice@example.com");
    assertThat(result.tokens()).isEqualTo(tokens);

    verify(authSessionPort)
        .save(
            eq(1L),
            eq(AuthSessionTokenKind.ACCESS),
            eq("access-token"),
            eq(NOW),
            eq(tokens.expiresAt()),
            eq("127.0.0.1"),
            eq("TestAgent"));
    verify(authSessionPort)
        .save(
            eq(1L),
            eq(AuthSessionTokenKind.REFRESH),
            eq("refresh-token"),
            eq(NOW),
            eq(tokens.refreshExpiresAt()),
            eq("127.0.0.1"),
            eq("TestAgent"));
  }

  @Test
  void execute_registeredUserNotFound_throwsInvalidAuthentication() {
    RegisterCommand command =
        new RegisterCommand(
            "alice",
            "Alice",
            null,
            "alice@example.com",
            "secret",
            UserType.INDIVIDUAL,
            null,
            null,
            null,
            null,
            "en",
            "127.0.0.1",
            "TestAgent");

    given(userRegistrationPort.register(command)).willReturn(1L);
    given(authUsersPort.findById(1L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(command))
        .isInstanceOf(InvalidAuthenticationException.class);

    verifyNoInteractions(authRbacPort, authTokenIssuerPort, authSessionPort, authClockPort);
  }
}

