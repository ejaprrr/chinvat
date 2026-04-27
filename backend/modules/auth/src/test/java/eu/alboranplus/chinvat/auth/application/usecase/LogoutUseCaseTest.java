package eu.alboranplus.chinvat.auth.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private LogoutUseCase sut;

  @Test
  void execute_revokesAccessToken() {
    given(authClockPort.now()).willReturn(NOW);
    LogoutCommand cmd = new LogoutCommand("the-access-token", "the-refresh-token");

    sut.execute(cmd);

    verify(authSessionPort).revokeByRawToken("the-access-token", NOW);
  }

  @Test
  void execute_revokesRefreshToken() {
    given(authClockPort.now()).willReturn(NOW);
    LogoutCommand cmd = new LogoutCommand("the-access-token", "the-refresh-token");

    sut.execute(cmd);

    verify(authSessionPort).revokeByRawToken("the-refresh-token", NOW);
  }

  @Test
  void execute_revokesBothTokens_exactlyTwoCalls() {
    given(authClockPort.now()).willReturn(NOW);
    LogoutCommand cmd = new LogoutCommand("access", "refresh");

    sut.execute(cmd);

    verify(authSessionPort, times(2)).revokeByRawToken(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }
}
