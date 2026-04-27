package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetAllUsersUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public GetAllUsersUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  public List<UserAccount> execute() {
    return usersRepositoryPort.findAll();
  }
}
