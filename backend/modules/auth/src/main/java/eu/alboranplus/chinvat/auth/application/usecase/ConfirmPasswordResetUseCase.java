package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmPasswordResetUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthPasswordResetTokenPort passwordResetTokenPort;
  private final AuthPasswordChangePort passwordChangePort;
  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public ConfirmPasswordResetUseCase(
      AuthUsersPort authUsersPort,
      AuthPasswordResetTokenPort passwordResetTokenPort,
      AuthPasswordChangePort passwordChangePort,
      AuthSessionPort authSessionPort,
      AuthClockPort authClockPort) {
    this.authUsersPort = authUsersPort;
    this.passwordResetTokenPort = passwordResetTokenPort;
    this.passwordChangePort = passwordChangePort;
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public void execute(ConfirmPasswordResetCommand command) {
    Instant now = authClockPort.now();
    Long userId =
      authUsersPort
        .findByEmail(command.email())
        .filter(user -> user.active())
        .map(user -> user.userId())
        .orElseThrow(() -> new InvalidAuthenticationException("Invalid or expired reset code"));

    passwordResetTokenPort
      .consume(userId, command.resetCode(), now)
      .orElseThrow(() -> new InvalidAuthenticationException("Invalid or expired reset code"));

    passwordChangePort.changePassword(userId, command.newPassword());

    // Security best practice: revoke all active sessions after password change.
    authSessionPort.revokeAllByUserId(userId, now);
  }
}

