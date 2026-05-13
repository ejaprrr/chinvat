package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.auth.application.command.CertificateLoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.usecase.CertificateLoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RegisterUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RequestPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.AuthChangePasswordUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ConfirmPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.GetMeUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ListSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ListSessionsPagedUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RevokeSessionUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutAllSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RefreshTokenUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ValidateTokenUseCase;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;

@Service
public class AuthFacadeService implements AuthFacade {

  private final LoginUseCase loginUseCase;
  private final CertificateLoginUseCase certificateLoginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final ValidateTokenUseCase validateTokenUseCase;
  private final RegisterUseCase registerUseCase;
  private final RequestPasswordResetUseCase requestPasswordResetUseCase;
  private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
  private final AuthChangePasswordUseCase changePasswordUseCase;
  private final GetMeUseCase getMeUseCase;
  private final ListSessionsUseCase listSessionsUseCase;
  private final ListSessionsPagedUseCase listSessionsPagedUseCase;
  private final RevokeSessionUseCase revokeSessionUseCase;
  private final LogoutAllSessionsUseCase logoutAllSessionsUseCase;
  private final AuditFacade auditFacade;

  public AuthFacadeService(
      LoginUseCase loginUseCase,
      CertificateLoginUseCase certificateLoginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase,
      ValidateTokenUseCase validateTokenUseCase,
      RegisterUseCase registerUseCase,
      RequestPasswordResetUseCase requestPasswordResetUseCase,
      ConfirmPasswordResetUseCase confirmPasswordResetUseCase,
      AuthChangePasswordUseCase changePasswordUseCase,
      GetMeUseCase getMeUseCase,
      ListSessionsUseCase listSessionsUseCase,
      ListSessionsPagedUseCase listSessionsPagedUseCase,
      RevokeSessionUseCase revokeSessionUseCase,
      LogoutAllSessionsUseCase logoutAllSessionsUseCase,
      AuditFacade auditFacade) {
    this.loginUseCase = loginUseCase;
    this.certificateLoginUseCase = certificateLoginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
    this.validateTokenUseCase = validateTokenUseCase;
    this.registerUseCase = registerUseCase;
    this.requestPasswordResetUseCase = requestPasswordResetUseCase;
    this.confirmPasswordResetUseCase = confirmPasswordResetUseCase;
    this.changePasswordUseCase = changePasswordUseCase;
    this.getMeUseCase = getMeUseCase;
    this.listSessionsUseCase = listSessionsUseCase;
    this.listSessionsPagedUseCase = listSessionsPagedUseCase;
    this.revokeSessionUseCase = revokeSessionUseCase;
    this.logoutAllSessionsUseCase = logoutAllSessionsUseCase;
    this.auditFacade = auditFacade;
  }

  @Override
  public AuthResult login(LoginCommand command) {
    AuthResult result = loginUseCase.execute(command);
    auditFacade.log(
        "AUTH_LOGIN_SUCCEEDED",
        result.email(),
        result.userId(),
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .build());
    return result;
  }

  @Override
  public AuthResult loginWithCertificate(CertificateLoginCommand command) {
    AuthResult result = certificateLoginUseCase.execute(command);
    auditFacade.log(
        "AUTH_MTLS_LOGIN_SUCCEEDED",
        result.email(),
        result.userId(),
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .add("thumbprintSha256", command.thumbprintSha256())
            .build());
    return result;
  }

  @Override
  public AuthResult refresh(RefreshCommand command) {
    AuthResult result = refreshTokenUseCase.execute(command);
    auditFacade.log(
        "AUTH_REFRESH_SUCCEEDED",
        result.email(),
        result.userId(),
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .build());
    return result;
  }

  @Override
  public void logout(LogoutCommand command) {
    logoutUseCase.execute(command);
  }

  @Override
  public Optional<TokenPrincipal> validateAccessToken(String rawAccessToken) {
    return validateTokenUseCase.execute(rawAccessToken);
  }

  @Override
  public AuthResult register(RegisterCommand command) {
    AuthResult result = registerUseCase.execute(command);
    auditFacade.log(
        "AUTH_REGISTER_SUCCEEDED",
        result.email(),
        result.userId(),
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .build());
    return result;
  }

  @Override
  public PasswordResetRequestResult requestPasswordReset(RequestPasswordResetCommand command) {
    PasswordResetRequestResult result = requestPasswordResetUseCase.execute(command);
    auditFacade.log(
        "AUTH_PASSWORD_RESET_REQUESTED",
        command.email(),
        null,
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .build());
    return result;
  }

  @Override
  public void confirmPasswordReset(ConfirmPasswordResetCommand command) {
    confirmPasswordResetUseCase.execute(command);
    auditFacade.log(
        "AUTH_PASSWORD_RESET_CONFIRMED",
        command.email(),
        null,
        AuditDetails.builder()
            .add("clientIp", command.clientIp())
            .add("userAgent", command.userAgent())
            .build());
  }

  @Override
  public void changePassword(TokenPrincipal principal, ChangePasswordCommand command) {
    changePasswordUseCase.execute(principal, command);
    auditFacade.log(
        "AUTH_PASSWORD_CHANGED",
        principal.email(),
        principal.userId(),
        AuditDetails.builder().build());
  }

  @Override
  public AuthMeView me(TokenPrincipal principal) {
    return getMeUseCase.execute(principal);
  }

  @Override
  public List<AuthSessionView> listSessions(TokenPrincipal principal) {
    return listSessionsUseCase.execute(principal);
  }

  @Override
  public void revokeSession(TokenPrincipal principal, UUID sessionId) {
    revokeSessionUseCase.execute(principal, sessionId);
    auditFacade.log(
        "AUTH_SESSION_REVOKED",
        principal.email(),
        principal.userId(),
        AuditDetails.builder().add("sessionId", sessionId).build());
  }

  @Override
  public void logoutAll(TokenPrincipal principal) {
    logoutAllSessionsUseCase.execute(principal);
    auditFacade.log(
        "AUTH_LOGOUT_ALL",
        principal.email(),
        principal.userId(),
        AuditDetails.builder().build());
  }

  @Override
  public PageResponse<AuthSessionView> listSessionsPaged(TokenPrincipal principal, PaginationRequest paginationRequest) {
    return listSessionsPagedUseCase.execute(principal, paginationRequest);
  }
}
