package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.usecase.LoginUseCase;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeService implements AuthFacade {

  private final LoginUseCase loginUseCase;

  public AuthFacadeService(LoginUseCase loginUseCase) {
    this.loginUseCase = loginUseCase;
  }

  @Override
  public AuthResult login(LoginCommand command) {
    return loginUseCase.execute(command);
  }
}
