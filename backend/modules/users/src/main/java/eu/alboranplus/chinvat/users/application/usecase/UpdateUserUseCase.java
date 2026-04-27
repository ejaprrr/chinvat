package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersClockPort usersClockPort;

  public UpdateUserUseCase(
      UsersRepositoryPort usersRepositoryPort, UsersClockPort usersClockPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersClockPort = usersClockPort;
  }

  @Transactional
  public UserAccount execute(Long id, UpdateUserCommand command) {
    UserAccount existing =
        usersRepositoryPort
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

    if (!existing.username().equals(command.username())
        && usersRepositoryPort.existsByUsername(command.username())) {
      throw new UserAlreadyExistsException("Username already taken: " + command.username());
    }

    UserAccount updated =
        existing.withUpdatedFields(
            command.username(),
            command.fullName(),
            command.phoneNumber(),
            command.userType(),
            command.accessLevel(),
            command.addressLine(),
            command.postalCode(),
            command.city(),
            command.country(),
            command.defaultLanguage(),
            usersClockPort.now());

    return usersRepositoryPort.save(updated);
  }
}
