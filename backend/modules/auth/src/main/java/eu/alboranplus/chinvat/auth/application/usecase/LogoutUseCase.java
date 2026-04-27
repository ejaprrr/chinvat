package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public LogoutUseCase(AuthSessionPort authSessionPort, AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public void execute(LogoutCommand command) {
    var now = authClockPort.now();
    authSessionPort.revokeByRawToken(command.accessToken(), now);
    authSessionPort.revokeByRawToken(command.refreshToken(), now);
  }
}
