package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import org.springframework.stereotype.Service;

@Service
public class VerifyUserPasswordUseCase {

  private final UsersRepositoryPort usersRepositoryPort;
  private final UsersPasswordPort usersPasswordPort;
  private final UsersPasswordHasherPort usersPasswordHasherPort;

  public VerifyUserPasswordUseCase(
      UsersRepositoryPort usersRepositoryPort,
      UsersPasswordPort usersPasswordPort,
      UsersPasswordHasherPort usersPasswordHasherPort) {
    this.usersRepositoryPort = usersRepositoryPort;
    this.usersPasswordPort = usersPasswordPort;
    this.usersPasswordHasherPort = usersPasswordHasherPort;
  }

  public boolean execute(String email, String rawPassword) {
    return usersRepositoryPort
        .findByEmail(UserEmail.of(email))
        .flatMap(user -> usersPasswordPort.findHashByUserId(user.id()))
        .map(hash -> usersPasswordHasherPort.matches(rawPassword, hash))
        .orElse(false);
  }
}

