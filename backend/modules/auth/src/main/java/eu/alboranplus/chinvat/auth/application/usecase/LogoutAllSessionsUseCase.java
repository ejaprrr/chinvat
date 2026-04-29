package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutAllSessionsUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public LogoutAllSessionsUseCase(AuthSessionPort authSessionPort, AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public void execute(TokenPrincipal principal) {
    Instant now = authClockPort.now();
    authSessionPort.revokeAllByUserId(principal.userId(), now);
  }
}

