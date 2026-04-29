package eu.alboranplus.chinvat.auth.infrastructure.integration;

import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUserRegistrationPort;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import org.springframework.stereotype.Component;

@Component
public class AuthUserRegistrationFacadeAdapter implements AuthUserRegistrationPort {

  private final UsersFacade usersFacade;

  public AuthUserRegistrationFacadeAdapter(UsersFacade usersFacade) {
    this.usersFacade = usersFacade;
  }

  @Override
  public Long register(RegisterCommand command) {
    CreateUserCommand createCommand =
        new CreateUserCommand(
            command.username(),
            command.fullName(),
            command.phoneNumber(),
            command.email(),
            command.rawPassword(),
            command.userType(),
            AccessLevel.NORMAL,
            command.addressLine(),
            command.postalCode(),
            command.city(),
            command.country(),
            command.defaultLanguage());

    return usersFacade.createUser(createCommand).id();
  }
}

