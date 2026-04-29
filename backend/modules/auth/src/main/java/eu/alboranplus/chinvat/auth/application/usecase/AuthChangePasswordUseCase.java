package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthChangePasswordUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthPasswordChangePort passwordChangePort;
  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public AuthChangePasswordUseCase(
      AuthUsersPort authUsersPort,
      AuthPasswordChangePort passwordChangePort,
      AuthSessionPort authSessionPort,
      AuthClockPort authClockPort) {
    this.authUsersPort = authUsersPort;
    this.passwordChangePort = passwordChangePort;
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public void execute(TokenPrincipal principal, ChangePasswordCommand command) {
    boolean validCurrentPassword =
        authUsersPort.verifyPassword(principal.email(), command.currentPassword());
    if (!validCurrentPassword) {
      throw new InvalidAuthenticationException("Current password is invalid");
    }

    Instant now = authClockPort.now();
    passwordChangePort.changePassword(principal.userId(), command.newPassword());
    authSessionPort.revokeAllByUserId(principal.userId(), now);
  }
}