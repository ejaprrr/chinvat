package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetUserByIdUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public GetUserByIdUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  public UserAccount execute(UUID id) {
    return usersRepositoryPort
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
  }
}
