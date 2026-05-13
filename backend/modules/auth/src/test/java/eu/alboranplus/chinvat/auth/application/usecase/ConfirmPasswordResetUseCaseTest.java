package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ConfirmPasswordResetUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPasswordResetTokenPort passwordResetTokenPort;
  @Mock private AuthPasswordChangePort passwordChangePort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private ConfirmPasswordResetUseCase sut;

  @Test
  void execute_validCode_changesPasswordAndRevokesSessions() {
    UUID uuid10 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    ConfirmPasswordResetCommand cmd =
        new ConfirmPasswordResetCommand(
            "alice@example.com", "482193", "NewPass123456!", "127.0.0.1", "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByEmail("alice@example.com"))
        .willReturn(Optional.of(new AuthUserProjection(uuid10, "alice@example.com", "Alice", Set.of("USER"), true)));
    given(passwordResetTokenPort.consume(uuid10, "482193", NOW)).willReturn(Optional.of(uuid10));

    sut.execute(cmd);

    verify(passwordChangePort).changePassword(uuid10, "NewPass123456!");
    verify(authSessionPort).revokeAllByUserId(uuid10, NOW);
  }

  @Test
  void execute_invalidCode_throwsInvalidAuthentication() {
    UUID uuid10 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    ConfirmPasswordResetCommand cmd =
        new ConfirmPasswordResetCommand(
            "alice@example.com", "000000", "NewPass123456!", "127.0.0.1", "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByEmail("alice@example.com"))
        .willReturn(Optional.of(new AuthUserProjection(uuid10, "alice@example.com", "Alice", Set.of("USER"), true)));
    given(passwordResetTokenPort.consume(uuid10, "000000", NOW)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class);

    verify(passwordChangePort, never()).changePassword(any(), any());
    verify(authSessionPort, never()).revokeAllByUserId(any(), any());
  }
}

