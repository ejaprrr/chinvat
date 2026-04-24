package eu.alboranplus.chinvat.users.application.port.out;

import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.util.Optional;

public interface UsersRepositoryPort {
  boolean existsByEmail(UserEmail email);

  Optional<UserAccount> findByEmail(UserEmail email);

  UserAccount save(UserAccount userAccount);
}
