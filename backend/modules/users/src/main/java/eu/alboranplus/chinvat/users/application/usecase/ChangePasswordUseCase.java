package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordUseCase {

  private static final String PASSWORD_ALGORITHM = "bcrypt";

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersPasswordPort usersPasswordPort;
  private final UsersPasswordHasherPort usersPasswordHasherPort;

  public ChangePasswordUseCase(
      UsersRepositoryPort usersRepositoryPort,
      UsersPasswordPort usersPasswordPort,
      UsersPasswordHasherPort usersPasswordHasherPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersPasswordPort = usersPasswordPort;
    this.usersPasswordHasherPort = usersPasswordHasherPort;
  }

  @Transactional
  public void execute(ChangePasswordCommand command) {
    UUID userId = command.userId();
    usersRepositoryPort
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

    // Hash raw password with current encoder (bcrypt now, PQC-ready later via strategy).
    String passwordHash = usersPasswordHasherPort.hash(command.rawPassword());

    // `UsersPasswordPort` persists hash + metadata; user clock kept for future improvements.
    usersPasswordPort.save(userId, passwordHash, PASSWORD_ALGORITHM);
  }
}

