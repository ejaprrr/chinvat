package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ListSessionsUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private ListSessionsUseCase sut;

  @Test
  void execute_returnsActiveSessions() {
    TokenPrincipal principal =
        new TokenPrincipal(1L, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));

    AuthSessionView access =
        new AuthSessionView(
            UUID.randomUUID(),
            1L,
            AuthSessionTokenKind.ACCESS,
            NOW.minusSeconds(10),
            NOW.plusSeconds(900),
            "127.0.0.1",
            "Agent");

    AuthSessionView refresh =
        new AuthSessionView(
            UUID.randomUUID(),
            1L,
            AuthSessionTokenKind.REFRESH,
            NOW.minusSeconds(9),
            NOW.plusSeconds(1200),
            "127.0.0.1",
            "Agent");

    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.listActiveSessionsByUserId(1L, NOW)).willReturn(List.of(access, refresh));

    List<AuthSessionView> sessions = sut.execute(principal);

    assertThat(sessions).hasSize(2);
    assertThat(sessions.get(0).tokenKind()).isEqualTo(AuthSessionTokenKind.ACCESS);
  }
}

