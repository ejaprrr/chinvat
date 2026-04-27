package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersPasswordPort usersPasswordPort;
  private final UsersPasswordHasherPort usersPasswordHasherPort;
  private final UsersClockPort usersClockPort;

  public CreateUserUseCase(
      UsersRepositoryPort usersRepositoryPort,
      UsersPasswordPort usersPasswordPort,
      UsersPasswordHasherPort usersPasswordHasherPort,
      UsersClockPort usersClockPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersPasswordPort = usersPasswordPort;
    this.usersPasswordHasherPort = usersPasswordHasherPort;
    this.usersClockPort = usersClockPort;
  }

  @Transactional
  public UserAccount execute(CreateUserCommand command) {
    UserEmail email = UserEmail.of(command.email());

    if (usersRepositoryPort.existsByEmail(email)) {
      throw new UserAlreadyExistsException("Email already registered: " + email.value());
    }
    if (usersRepositoryPort.existsByUsername(command.username())) {
      throw new UserAlreadyExistsException("Username already taken: " + command.username());
    }

    Instant now = usersClockPort.now();
    UserAccount userAccount =
        UserAccount.newUser(
            command.username(),
            command.fullName(),
            command.phoneNumber(),
            email,
            command.userType(),
            command.accessLevel(),
            command.addressLine(),
            command.postalCode(),
            command.city(),
            command.country(),
            command.defaultLanguage(),
            now);

    UserAccount saved = usersRepositoryPort.save(userAccount);

    String passwordHash = usersPasswordHasherPort.hash(command.rawPassword());
    usersPasswordPort.save(saved.id(), passwordHash, "bcrypt");

    return saved;
  }
}

