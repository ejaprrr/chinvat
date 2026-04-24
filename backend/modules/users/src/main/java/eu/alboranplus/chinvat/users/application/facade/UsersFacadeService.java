package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.usecase.CreateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsersFacadeService implements UsersFacade {

  private final CreateUserUseCase createUserUseCase;
  private final GetUserSecurityViewUseCase getUserSecurityViewUseCase;
  private final VerifyUserPasswordUseCase verifyUserPasswordUseCase;

  public UsersFacadeService(
      CreateUserUseCase createUserUseCase,
      GetUserSecurityViewUseCase getUserSecurityViewUseCase,
      VerifyUserPasswordUseCase verifyUserPasswordUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.getUserSecurityViewUseCase = getUserSecurityViewUseCase;
    this.verifyUserPasswordUseCase = verifyUserPasswordUseCase;
  }

  @Override
  public UserView createUser(CreateUserCommand command) {
    UserAccount userAccount = createUserUseCase.execute(command);
    return new UserView(
        userAccount.id(),
        userAccount.email().value(),
        userAccount.displayName(),
        userAccount.roles(),
        userAccount.active());
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewByEmail(String email) {
    return getUserSecurityViewUseCase.execute(email);
  }

  @Override
  public boolean verifyPassword(String email, String rawPassword) {
    return verifyUserPasswordUseCase.execute(email, rawPassword);
  }
}
