package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import java.util.Optional;

public interface UsersFacade {
  UserView createUser(CreateUserCommand command);

  Optional<UserSecurityView> findSecurityViewByEmail(String email);

  boolean verifyPassword(String email, String rawPassword);
}
