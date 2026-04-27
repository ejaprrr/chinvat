package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GetUserSecurityViewUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public GetUserSecurityViewUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  public Optional<UserSecurityView> execute(String email) {
    return usersRepositoryPort.findByEmail(UserEmail.of(email)).map(this::toSecurityView);
  }

  public Optional<UserSecurityView> executeById(Long id) {
    return usersRepositoryPort.findById(id).map(this::toSecurityView);
  }

  private UserSecurityView toSecurityView(UserAccount user) {
    return new UserSecurityView(
        user.id(),
        user.email().value(),
        user.fullName(),
        Set.of(user.accessLevel().name()),
        true);
  }
}

