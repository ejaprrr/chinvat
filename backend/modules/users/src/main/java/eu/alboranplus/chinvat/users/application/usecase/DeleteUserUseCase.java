package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public DeleteUserUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  @Transactional
  public void execute(UUID id) {
    if (!usersRepositoryPort.findById(id).isPresent()) {
      throw new UserNotFoundException("User not found: " + id);
    }
    usersRepositoryPort.deleteById(id);
  }
}
