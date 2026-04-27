package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.usecase.CreateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.DeleteUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserByIdUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.UpdateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsersFacadeService implements UsersFacade {

  private final CreateUserUseCase createUserUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final GetUserSecurityViewUseCase getUserSecurityViewUseCase;
  private final VerifyUserPasswordUseCase verifyUserPasswordUseCase;

  public UsersFacadeService(
      CreateUserUseCase createUserUseCase,
      GetUserByIdUseCase getUserByIdUseCase,
      GetAllUsersUseCase getAllUsersUseCase,
      UpdateUserUseCase updateUserUseCase,
      DeleteUserUseCase deleteUserUseCase,
      GetUserSecurityViewUseCase getUserSecurityViewUseCase,
      VerifyUserPasswordUseCase verifyUserPasswordUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.getUserByIdUseCase = getUserByIdUseCase;
    this.getAllUsersUseCase = getAllUsersUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.getUserSecurityViewUseCase = getUserSecurityViewUseCase;
    this.verifyUserPasswordUseCase = verifyUserPasswordUseCase;
  }

  @Override
  public UserView createUser(CreateUserCommand command) {
    return toView(createUserUseCase.execute(command));
  }

  @Override
  public UserView getUserById(Long id) {
    return toView(getUserByIdUseCase.execute(id));
  }

  @Override
  public List<UserView> getAllUsers() {
    return getAllUsersUseCase.execute().stream().map(this::toView).toList();
  }

  @Override
  public UserView updateUser(Long id, UpdateUserCommand command) {
    return toView(updateUserUseCase.execute(id, command));
  }

  @Override
  public void deleteUser(Long id) {
    deleteUserUseCase.execute(id);
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewByEmail(String email) {
    return getUserSecurityViewUseCase.execute(email);
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewById(Long id) {
    return getUserSecurityViewUseCase.executeById(id);
  }

  @Override
  public boolean verifyPassword(String email, String rawPassword) {
    return verifyUserPasswordUseCase.execute(email, rawPassword);
  }

  private UserView toView(UserAccount user) {
    return new UserView(
        user.id(),
        user.username(),
        user.fullName(),
        user.phoneNumber(),
        user.email().value(),
        user.userType(),
        user.accessLevel(),
        user.addressLine(),
        user.postalCode(),
        user.city(),
        user.country(),
        user.defaultLanguage());
  }
}

