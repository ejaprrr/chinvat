package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
  private static final Instant ACCESS_EXP = NOW.plusSeconds(900);
  private static final Instant REFRESH_EXP = NOW.plusSeconds(1_209_600);

  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPermissionService authPermissionService;
  @Mock private AuthTokenIssuerPort authTokenIssuerPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private RefreshTokenUseCase sut;

  private final AuthUserProjection activeUser =
      new AuthUserProjection(1L, "alice@example.com", "Alice", Set.of("USER"), true);

  private final IssuedTokenPair newTokens =
      new IssuedTokenPair("new-access", "new-refresh", ACCESS_EXP, REFRESH_EXP);

  @Test
  void execute_validRefreshToken_rotatesTokensAndReturnsResult() {
    RefreshCommand cmd = new RefreshCommand("old-refresh", "127.0.0.1", "Agent");
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("old-refresh", NOW)).willReturn(Optional.of(1L));
    given(authUsersPort.findById(1L)).willReturn(Optional.of(activeUser));
    given(authPermissionService.resolvePermissions(1L, Set.of("USER")))
      .willReturn(Set.of("PROFILE:READ"));
    given(authTokenIssuerPort.issue(1L, "alice@example.com", NOW)).willReturn(newTokens);

    AuthResult result = sut.execute(cmd);

    assertThat(result.tokens().accessToken()).isEqualTo("new-access");
    assertThat(result.tokens().refreshToken()).isEqualTo("new-refresh");
    // Old refresh token must be revoked
    verify(authSessionPort).revokeByRawToken("old-refresh", NOW);
    // Two new sessions saved (access + refresh)
    verify(authSessionPort, times(2)).save(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void execute_expiredOrRevokedRefreshToken_throwsInvalidAuthentication() {
    RefreshCommand cmd = new RefreshCommand("expired-token", "127.0.0.1", "Agent");
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("expired-token", NOW)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class)
        .hasMessageContaining("Invalid or expired refresh token");

    verify(authSessionPort, never()).revokeByRawToken(any(), any());
    verify(authSessionPort, never()).save(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void execute_userInactive_throwsInvalidAuthentication() {
    AuthUserProjection inactive =
        new AuthUserProjection(2L, "bob@example.com", "Bob", Set.of("USER"), false);
    RefreshCommand cmd = new RefreshCommand("valid-refresh", "127.0.0.1", "Agent");
    given(authClockPort.now()).willReturn(NOW);
    given(authSessionPort.findActiveUserId("valid-refresh", NOW)).willReturn(Optional.of(2L));
    given(authUsersPort.findById(2L)).willReturn(Optional.of(inactive));

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(InvalidAuthenticationException.class)
        .hasMessageContaining("inactive");
  }
}
