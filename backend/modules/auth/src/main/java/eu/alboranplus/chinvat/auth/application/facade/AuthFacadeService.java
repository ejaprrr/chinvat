package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.usecase.LoginUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.LogoutUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.RefreshTokenUseCase;
import eu.alboranplus.chinvat.auth.application.usecase.ValidateTokenUseCase;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeService implements AuthFacade {

  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final ValidateTokenUseCase validateTokenUseCase;

  public AuthFacadeService(
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase,
      ValidateTokenUseCase validateTokenUseCase) {
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
    this.validateTokenUseCase = validateTokenUseCase;
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
}
