package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPasswordChangePort passwordChangePort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private AuthChangePasswordUseCase sut;

  @Test
  void execute_validCurrentPassword_changesPasswordAndRevokesSessions() {
    UUID uuid10 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    TokenPrincipal principal =
        new TokenPrincipal(uuid10, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));
    ChangePasswordCommand command = new ChangePasswordCommand("CurrentPass123!", "NewPass123456!");

    given(authUsersPort.verifyPassword("alice@example.com", "CurrentPass123!")).willReturn(true);
    given(authClockPort.now()).willReturn(NOW);

    sut.execute(principal, command);

    verify(passwordChangePort).changePassword(uuid10, "NewPass123456!");
    verify(authSessionPort).revokeAllByUserId(uuid10, NOW);
  }

  @Test
  void execute_invalidCurrentPassword_throwsUnauthorized() {
    UUID uuid10 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    TokenPrincipal principal =
        new TokenPrincipal(uuid10, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));
    ChangePasswordCommand command = new ChangePasswordCommand("WrongPass123!", "NewPass123456!");

    given(authUsersPort.verifyPassword("alice@example.com", "WrongPass123!")).willReturn(false);

    assertThatThrownBy(() -> sut.execute(principal, command))
        .isInstanceOf(InvalidAuthenticationException.class)
        .hasMessage("Current password is invalid");

    verify(passwordChangePort, never()).changePassword(any(), any());
    verify(authSessionPort, never()).revokeAllByUserId(any(), any());
  }
}