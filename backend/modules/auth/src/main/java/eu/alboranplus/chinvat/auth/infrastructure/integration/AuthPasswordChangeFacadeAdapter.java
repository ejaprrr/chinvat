package eu.alboranplus.chinvat.auth.infrastructure.integration;

import eu.alboranplus.chinvat.auth.application.port.out.AuthPasswordChangePort;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import org.springframework.stereotype.Component;

@Component
public class AuthPasswordChangeFacadeAdapter implements AuthPasswordChangePort {

  private final UsersFacade usersFacade;

  public AuthPasswordChangeFacadeAdapter(UsersFacade usersFacade) {
    this.usersFacade = usersFacade;
  }

  @Override
  public void changePassword(Long userId, String rawPassword) {
    usersFacade.changePassword(userId, rawPassword);
  }
}

