package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import java.util.UUID;

public interface AuthUserRegistrationPort {
  UUID register(RegisterCommand command);
}

