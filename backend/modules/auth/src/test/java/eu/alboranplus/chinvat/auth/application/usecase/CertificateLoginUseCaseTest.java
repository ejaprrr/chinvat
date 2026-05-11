package eu.alboranplus.chinvat.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.auth.application.command.CertificateLoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CertificateLoginUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final Instant ACCESS_EXP = NOW.plusSeconds(900);
  private static final Instant REFRESH_EXP = NOW.plusSeconds(1_209_600);

  @Mock private AuthUsersPort authUsersPort;
  @Mock private AuthPermissionService authPermissionService;
  @Mock private AuthTokenIssuerPort authTokenIssuerPort;
  @Mock private AuthSessionPort authSessionPort;
  @Mock private AuthClockPort authClockPort;

  @InjectMocks private CertificateLoginUseCase sut;

  private static final UUID UUID_7 = UUID.fromString("00000000-0000-0000-0000-000000000007");

  private final AuthUserProjection activeUser =
      new AuthUserProjection(UUID_7, "fnmt@example.com", "FNMT User", Set.of("USER"), true);

  private final IssuedTokenPair tokens =
      new IssuedTokenPair("access-token", "refresh-token", ACCESS_EXP, REFRESH_EXP);

  @Test
  void execute_registeredCertificate_returnsAuthResult() {
    CertificateLoginCommand command =
        new CertificateLoginCommand("ABCDEF", "127.0.0.1", "curl/8");
    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByCertificateThumbprint("ABCDEF", NOW))
        .willReturn(Optional.of(activeUser));
    given(authPermissionService.resolvePermissions(UUID_7, Set.of("USER")))
        .willReturn(Set.of("PROFILE:READ"));
    given(authTokenIssuerPort.issue(UUID_7, "fnmt@example.com", NOW)).willReturn(tokens);

    AuthResult result = sut.execute(command);

    assertThat(result.userId()).isEqualTo(UUID_7);
    assertThat(result.permissions()).containsExactly("PROFILE:READ");
    verify(authSessionPort)
        .save(
            eq(UUID_7),
            eq(AuthSessionTokenKind.ACCESS),
            eq("access-token"),
            eq(NOW),
            eq(ACCESS_EXP),
            eq("127.0.0.1"),
            eq("curl/8"));
    verify(authSessionPort)
        .save(
            eq(UUID_7),
            eq(AuthSessionTokenKind.REFRESH),
            eq("refresh-token"),
            eq(NOW),
            eq(REFRESH_EXP),
            eq("127.0.0.1"),
            eq("curl/8"));
  }

  @Test
  void execute_unknownCertificate_throwsInvalidAuthentication() {
    CertificateLoginCommand command =
        new CertificateLoginCommand("MISSING", "127.0.0.1", "curl/8");
    given(authClockPort.now()).willReturn(NOW);
    given(authUsersPort.findByCertificateThumbprint("MISSING", NOW)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(command))
        .isInstanceOf(InvalidAuthenticationException.class)
        .hasMessageContaining("No active user is registered for this certificate");

    verify(authSessionPort, never()).save(any(), any(), any(), any(), any(), any(), any());
  }
}