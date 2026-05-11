package eu.alboranplus.chinvat.auth.application.facade;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.alboranplus.chinvat.auth.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.usecase.AuthChangePasswordUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.CertificateLoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ConfirmPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.GetMeUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ListSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ListSessionsPagedUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutAllSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RefreshTokenUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RegisterUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RequestPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RevokeSessionUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ValidateTokenUseCase;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthFacadeServiceTest {

  @Mock private LoginUseCase loginUseCase;
    @Mock private CertificateLoginUseCase certificateLoginUseCase;
  @Mock private RefreshTokenUseCase refreshTokenUseCase;
  @Mock private LogoutUseCase logoutUseCase;
  @Mock private ValidateTokenUseCase validateTokenUseCase;
  @Mock private RegisterUseCase registerUseCase;
  @Mock private RequestPasswordResetUseCase requestPasswordResetUseCase;
  @Mock private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
  @Mock private AuthChangePasswordUseCase changePasswordUseCase;
  @Mock private GetMeUseCase getMeUseCase;
  @Mock private ListSessionsUseCase listSessionsUseCase;
  @Mock private ListSessionsPagedUseCase listSessionsPagedUseCase;
  @Mock private RevokeSessionUseCase revokeSessionUseCase;
  @Mock private LogoutAllSessionsUseCase logoutAllSessionsUseCase;
  @Mock private AuditFacade auditFacade;

  private AuthFacadeService sut;

  @BeforeEach
  void setUp() {
    sut =
        new AuthFacadeService(
            loginUseCase,
            certificateLoginUseCase,
            refreshTokenUseCase,
            logoutUseCase,
            validateTokenUseCase,
            registerUseCase,
            requestPasswordResetUseCase,
            confirmPasswordResetUseCase,
            changePasswordUseCase,
            getMeUseCase,
            listSessionsUseCase,
            listSessionsPagedUseCase,
            revokeSessionUseCase,
            logoutAllSessionsUseCase,
            auditFacade);
  }

  @Test
  void login_logsSharedAuditEvent() {
    UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    LoginCommand command = new LoginCommand("alice@example.com", "secret", "127.0.0.1", "Agent");
    AuthResult result =
        new AuthResult(
            uuid1,
            "alice@example.com",
            "Alice",
            Set.of("USER"),
            Set.of("PROFILE:READ"),
            new IssuedTokenPair(
                "access-token",
                "refresh-token",
                Instant.parse("2026-01-01T00:15:00Z"),
                Instant.parse("2026-01-15T00:00:00Z")));
    given(loginUseCase.execute(command)).willReturn(result);

    sut.login(command);

    then(auditFacade)
        .should()
        .log(
            eq("AUTH_LOGIN_SUCCEEDED"),
            eq("alice@example.com"),
            eq(uuid1),
            eq(Map.of("clientIp", "127.0.0.1", "userAgent", "Agent")));
  }

  @Test
  void requestPasswordReset_logsSharedAuditEvent() {
    var command =
        new eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand(
            "alice@example.com", "127.0.0.1", "Agent", false);
    given(requestPasswordResetUseCase.execute(command))
        .willReturn(new PasswordResetRequestResult(null, Instant.parse("2026-01-01T00:05:00Z")));

    sut.requestPasswordReset(command);

    then(auditFacade)
        .should()
        .log(
            eq("AUTH_PASSWORD_RESET_REQUESTED"),
            eq("alice@example.com"),
            eq(null),
            eq(Map.of("clientIp", "127.0.0.1", "userAgent", "Agent")));
  }

  @Test
  void logoutAll_logsSharedAuditEvent() {
    UUID uuid7 = UUID.fromString("00000000-0000-0000-0000-000000000007");
    TokenPrincipal principal =
        new TokenPrincipal(uuid7, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));

    sut.logoutAll(principal);

    then(auditFacade)
        .should()
        .log(eq("AUTH_LOGOUT_ALL"), eq("alice@example.com"), eq(uuid7), eq(Map.of()));
  }
}