package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GetUserSecurityViewUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public GetUserSecurityViewUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  public Optional<UserSecurityView> execute(String email) {
    return usersRepositoryPort
        .findByEmail(UserEmail.of(email))
        .map(
            user ->
                new UserSecurityView(
                    user.id(),
                    user.email().value(),
                    user.displayName(),
                    user.roles(),
                    user.active()));
  }
}
