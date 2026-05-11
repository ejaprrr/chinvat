package eu.alboranplus.chinvat.users.application.port.out;

import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsersRepositoryPort {
  boolean existsByEmail(UserEmail email);

  boolean existsByUsername(String username);

  Optional<UserAccount> findByEmail(UserEmail email);

  Optional<UserAccount> findById(Long id);

  Optional<UserAccount> findByCertificateThumbprint(String thumbprintSha256, Instant now);

  List<UserAccount> findAll();

  Page<UserAccount> findAll(Pageable pageable);

  UserAccount save(UserAccount userAccount);

  void deleteById(Long id);
}
