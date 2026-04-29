package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;

public interface AuthUserRegistrationPort {
  Long register(RegisterCommand command);
}

