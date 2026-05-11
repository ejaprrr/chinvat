package eu.alboranplus.chinvat.auth.application.usecase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
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
class LogoutAllSessionsUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private LogoutAllSessionsUseCase sut;

  @Test
  void execute_revokesAllUserSessions() {
    UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    TokenPrincipal principal =
        new TokenPrincipal(uuid1, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));

    given(authClockPort.now()).willReturn(NOW);

    sut.execute(principal);

    verify(authSessionPort).revokeAllByUserId(uuid1, NOW);
  }
}

