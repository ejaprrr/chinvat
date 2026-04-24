package eu.alboranplus.chinvat.auth.application.facade;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;

public interface AuthFacade {
  AuthResult login(LoginCommand command);
}
