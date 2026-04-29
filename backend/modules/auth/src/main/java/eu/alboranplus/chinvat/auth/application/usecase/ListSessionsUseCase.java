package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSessionsUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public ListSessionsUseCase(AuthSessionPort authSessionPort, AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional(readOnly = true)
  public List<AuthSessionView> execute(TokenPrincipal principal) {
    Instant now = authClockPort.now();
    return authSessionPort.listActiveSessionsByUserId(principal.userId(), now);
  }
}

