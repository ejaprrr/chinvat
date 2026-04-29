package eu.alboranplus.chinvat.auth.application.facade;

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
import eu.alboranplus.chinvat.auth.application.usecase.LoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RegisterUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RequestPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.AuthChangePasswordUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ConfirmPasswordResetUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.GetMeUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ListSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RevokeSessionUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutAllSessionsUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RefreshTokenUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ValidateTokenUseCase;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeService implements AuthFacade {

  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final ValidateTokenUseCase validateTokenUseCase;
  private final RegisterUseCase registerUseCase;
  private final RequestPasswordResetUseCase requestPasswordResetUseCase;
  private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
  private final AuthChangePasswordUseCase changePasswordUseCase;
  private final GetMeUseCase getMeUseCase;
  private final ListSessionsUseCase listSessionsUseCase;
  private final RevokeSessionUseCase revokeSessionUseCase;
  private final LogoutAllSessionsUseCase logoutAllSessionsUseCase;

  public AuthFacadeService(
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase,
      ValidateTokenUseCase validateTokenUseCase,
      RegisterUseCase registerUseCase,
      RequestPasswordResetUseCase requestPasswordResetUseCase,
      ConfirmPasswordResetUseCase confirmPasswordResetUseCase,
      AuthChangePasswordUseCase changePasswordUseCase,
      GetMeUseCase getMeUseCase,
      ListSessionsUseCase listSessionsUseCase,
      RevokeSessionUseCase revokeSessionUseCase,
      LogoutAllSessionsUseCase logoutAllSessionsUseCase) {
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
    this.validateTokenUseCase = validateTokenUseCase;
    this.registerUseCase = registerUseCase;
    this.requestPasswordResetUseCase = requestPasswordResetUseCase;
    this.confirmPasswordResetUseCase = confirmPasswordResetUseCase;
    this.changePasswordUseCase = changePasswordUseCase;
    this.getMeUseCase = getMeUseCase;
    this.listSessionsUseCase = listSessionsUseCase;
    this.revokeSessionUseCase = revokeSessionUseCase;
    this.logoutAllSessionsUseCase = logoutAllSessionsUseCase;
  }

  @Override
  public AuthResult login(LoginCommand command) {
    return loginUseCase.execute(command);
  }

  @Override
  public AuthResult refresh(RefreshCommand command) {
    return refreshTokenUseCase.execute(command);
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
    return registerUseCase.execute(command);
  }

  @Override
  public PasswordResetRequestResult requestPasswordReset(RequestPasswordResetCommand command) {
    return requestPasswordResetUseCase.execute(command);
  }

  @Override
  public void confirmPasswordReset(ConfirmPasswordResetCommand command) {
    confirmPasswordResetUseCase.execute(command);
  }

  @Override
  public void changePassword(TokenPrincipal principal, ChangePasswordCommand command) {
    changePasswordUseCase.execute(principal, command);
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
  }

  @Override
  public void logoutAll(TokenPrincipal principal) {
    logoutAllSessionsUseCase.execute(principal);
  }
}
