package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ConfirmPasswordResetUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthPasswordResetTokenPort passwordResetTokenPort;
  @Mock private AuthPasswordChangePort passwordChangePort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private ConfirmPasswordResetUseCase sut;

  @Test
  void execute_validToken_changesPasswordAndRevokesSessions() {
    ConfirmPasswordResetCommand cmd =
        new ConfirmPasswordResetCommand("reset-token", "NewPass123456!", "127.0.0.1", "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(passwordResetTokenPort.consume("reset-token", NOW)).willReturn(Optional.of(10L));

    sut.execute(cmd);

    verify(passwordChangePort).changePassword(10L, "NewPass123456!");
    verify(authSessionPort).revokeAllByUserId(10L, NOW);
  }

  @Test
  void execute_invalidToken_throwsInvalidAuthentication() {
    ConfirmPasswordResetCommand cmd =
        new ConfirmPasswordResetCommand("bad-token", "NewPass123456!", "127.0.0.1", "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(passwordResetTokenPort.consume("bad-token", NOW)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class);

    verify(passwordChangePort, never()).changePassword(any(), any());
    verify(authSessionPort, never()).revokeAllByUserId(any(), any());
  }
}

