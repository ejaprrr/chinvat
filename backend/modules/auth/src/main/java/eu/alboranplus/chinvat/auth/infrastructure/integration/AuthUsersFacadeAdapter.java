package eu.alboranplus.chinvat.auth.infrastructure.integration;

import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuthUsersFacadeAdapter implements AuthUsersPort {

  private final UsersFacade usersFacade;

  public AuthUsersFacadeAdapter(UsersFacade usersFacade) {
    this.usersFacade = usersFacade;
  }

  @Override
  public Optional<AuthUserProjection> findByEmail(String email) {
    return usersFacade.findSecurityViewByEmail(email).map(this::mapUser);
  }

  @Override
  public Optional<AuthUserProjection> findById(Long userId) {
    return usersFacade.findSecurityViewById(userId).map(this::mapUser);
  }

  @Override
  public boolean verifyPassword(String email, String rawPassword) {
    return usersFacade.verifyPassword(email, rawPassword);
  }

  private AuthUserProjection mapUser(UserSecurityView user) {
    return new AuthUserProjection(
        user.id(), user.email(), user.displayName(), user.roles(), user.active());
  }
}
