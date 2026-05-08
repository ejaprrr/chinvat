package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRoleRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GetUserSecurityViewUseCase {

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersRoleRepositoryPort usersRoleRepositoryPort;

  public GetUserSecurityViewUseCase(
      UsersRepositoryPort usersRepositoryPort, UsersRoleRepositoryPort usersRoleRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersRoleRepositoryPort = usersRoleRepositoryPort;
  }

  public Optional<UserSecurityView> execute(String email) {
    return usersRepositoryPort.findByEmail(UserEmail.of(email)).map(this::toSecurityView);
  }

  public Optional<UserSecurityView> executeById(Long id) {
    return usersRepositoryPort.findById(id).map(this::toSecurityView);
  }

  public Optional<UserSecurityView> executeByCertificateThumbprint(
      String thumbprintSha256, Instant now) {
    return usersRepositoryPort.findByCertificateThumbprint(thumbprintSha256, now).map(this::toSecurityView);
  }

  private UserSecurityView toSecurityView(UserAccount user) {
    Set<String> roles = new HashSet<>();
    roles.add(user.accessLevel().name());
    roles.addAll(usersRoleRepositoryPort.findRoleNamesByUserId(user.id()));

    return new UserSecurityView(
        user.id(),
        user.email().value(),
        user.fullName(),
        Set.copyOf(roles),
        true);
  }
}

