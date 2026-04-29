package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.domain.exception.AuthResourceNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeSessionUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public RevokeSessionUseCase(AuthSessionPort authSessionPort, AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public void execute(TokenPrincipal principal, UUID sessionId) {
    Instant now = authClockPort.now();

    AuthSessionView session =
        authSessionPort
            .findActiveSessionById(sessionId, now)
            .orElseThrow(() -> new AuthResourceNotFoundException("Session not found"));

    // Prevent users from revoking sessions they don't own.
    if (!session.userId().equals(principal.userId())) {
      throw new AuthResourceNotFoundException("Session not found");
    }

    authSessionPort.revokeActiveSessionById(sessionId, now);
  }
}

