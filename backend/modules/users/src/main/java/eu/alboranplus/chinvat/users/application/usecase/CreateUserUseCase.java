package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

  private static final Set<String> DEFAULT_ROLES = Set.of("USER");

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersPasswordHasherPort usersPasswordHasherPort;
  private final UsersClockPort usersClockPort;

  public CreateUserUseCase(
      UsersRepositoryPort usersRepositoryPort,
      UsersPasswordHasherPort usersPasswordHasherPort,
      UsersClockPort usersClockPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersPasswordHasherPort = usersPasswordHasherPort;
    this.usersClockPort = usersClockPort;
  }

  public UserAccount execute(CreateUserCommand command) {
    UserEmail email = UserEmail.of(command.email());

    if (usersRepositoryPort.existsByEmail(email)) {
      throw new UserAlreadyExistsException("User already exists for email " + email.value());
    }

    Set<String> roles = command.roles().isEmpty() ? DEFAULT_ROLES : command.roles();
    String passwordHash = usersPasswordHasherPort.hash(command.rawPassword());
    Instant now = usersClockPort.now();

    UserAccount userAccount =
        UserAccount.newUser(email, command.displayName(), passwordHash, roles, now);

    return usersRepositoryPort.save(userAccount);
  }
}
