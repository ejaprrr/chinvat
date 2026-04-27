package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import java.util.Optional;

public interface AuthFacade {
  AuthResult login(LoginCommand command);

  AuthResult refresh(RefreshCommand command);

  void logout(LogoutCommand command);

  Optional<TokenPrincipal> validateAccessToken(String rawAccessToken);
}
