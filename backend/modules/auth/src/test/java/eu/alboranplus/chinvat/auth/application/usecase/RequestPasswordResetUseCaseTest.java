package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordResetTokenPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRecoveryTokenGeneratorPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final Duration TTL = Duration.ofMinutes(30);

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthRecoveryTokenGeneratorPort tokenGeneratorPort;
  @Mock private AuthPasswordResetTokenPort passwordResetTokenPort;
  @Mock private AuthClockPort authClockPort;

  @Test
  void execute_knownEmail_generatesAndStoresResetToken() {
    RequestPasswordResetUseCase sut =
        new RequestPasswordResetUseCase(
            authUsersPort, tokenGeneratorPort, passwordResetTokenPort, authClockPort, TTL);

    AuthUserProjection user =
        new AuthUserProjection(10L, "alice@example.com", "Alice", Set.of("USER"), true);

    RequestPasswordResetCommand cmd =
        new RequestPasswordResetCommand("alice@example.com", "127.0.0.1", "Agent", true);

    Instant expiresAt = NOW.plus(TTL);

    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByEmail("alice@example.com")).willReturn(Optional.of(user));
    given(tokenGeneratorPort.generateToken(10L, "alice@example.com", expiresAt))
        .willReturn("raw-reset-token");

    PasswordResetRequestResult result = sut.execute(cmd);

    assertThat(result.resetToken()).isEqualTo("raw-reset-token");
    assertThat(result.requestedAt()).isEqualTo(NOW);

    verify(passwordResetTokenPort)
        .save(eq(10L), eq("raw-reset-token"), eq(NOW), eq(expiresAt), eq("127.0.0.1"), eq("Agent"));
  }

  @Test
  void execute_unknownEmail_returnsNullTokenAndDoesNotStoreAnything() {
    RequestPasswordResetUseCase sut =
        new RequestPasswordResetUseCase(
            authUsersPort, tokenGeneratorPort, passwordResetTokenPort, authClockPort, TTL);

    RequestPasswordResetCommand cmd =
        new RequestPasswordResetCommand("ghost@example.com", "127.0.0.1", "Agent", true);

    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByEmail("ghost@example.com")).willReturn(Optional.empty());

    PasswordResetRequestResult result = sut.execute(cmd);

    assertThat(result.resetToken()).isNull();
    assertThat(result.requestedAt()).isEqualTo(NOW);

    verifyNoInteractions(tokenGeneratorPort, passwordResetTokenPort);
  }
}

