package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface AuthFacade {
  AuthResult login(LoginCommand command);

  AuthResult refresh(RefreshCommand command);

  void logout(LogoutCommand command);

  Optional<TokenPrincipal> validateAccessToken(String rawAccessToken);

  AuthResult register(RegisterCommand command);

  PasswordResetRequestResult requestPasswordReset(RequestPasswordResetCommand command);

  void confirmPasswordReset(ConfirmPasswordResetCommand command);

  void changePassword(TokenPrincipal principal, ChangePasswordCommand command);

  AuthMeView me(TokenPrincipal principal);

  List<AuthSessionView> listSessions(TokenPrincipal principal);

  void revokeSession(TokenPrincipal principal, UUID sessionId);

  void logoutAll(TokenPrincipal principal);
}
