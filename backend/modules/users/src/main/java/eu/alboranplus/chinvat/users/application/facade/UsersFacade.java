package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import java.util.List;
import java.util.Optional;

public interface UsersFacade {
  UserView createUser(CreateUserCommand command);

  UserView getUserById(Long id);

  List<UserView> getAllUsers();

  UserView updateUser(Long id, UpdateUserCommand command);

  void deleteUser(Long id);

  Optional<UserSecurityView> findSecurityViewByEmail(String email);

  Optional<UserSecurityView> findSecurityViewById(Long id);

  boolean verifyPassword(String email, String rawPassword);
}

