package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.domain.exception.AuthResourceNotFoundException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
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
class RevokeSessionUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private RevokeSessionUseCase sut;

  @Test
  void execute_revokesSessionWhenOwned() {
    UUID userId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID userId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    TokenPrincipal principal =
        new TokenPrincipal(userId1, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));
    UUID sessionId = UUID.randomUUID();

    AuthSessionView session =
        new AuthSessionView(
            sessionId,
            userId1,
            AuthSessionTokenKind.ACCESS,
            NOW.minusSeconds(10),
            NOW.plusSeconds(900),
            "127.0.0.1",
            "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveSessionById(sessionId, NOW)).willReturn(Optional.of(session));

    sut.execute(principal, sessionId);

    verify(authSessionPort).revokeActiveSessionById(sessionId, NOW);
  }

  @Test
  void execute_sessionNotOwned_throwsNotFound() {
    UUID userId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID userId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    TokenPrincipal principal =
        new TokenPrincipal(userId1, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));
    UUID sessionId = UUID.randomUUID();

    AuthSessionView session =
        new AuthSessionView(
            sessionId,
            userId2,
            AuthSessionTokenKind.ACCESS,
            NOW.minusSeconds(10),
            NOW.plusSeconds(900),
            "127.0.0.1",
            "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveSessionById(sessionId, NOW)).willReturn(Optional.of(session));

    assertThatThrownBy(() -> sut.execute(principal, sessionId))
        .isInstanceOf(AuthResourceNotFoundException.class);

    verify(authSessionPort, never()).revokeActiveSessionById(sessionId, NOW);
  }
}

