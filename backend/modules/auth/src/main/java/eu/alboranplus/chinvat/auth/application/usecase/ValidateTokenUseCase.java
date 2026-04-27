package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidateTokenUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthUsersPort authUsersPort;
  private final AuthRbacPort authRbacPort;
  private final AuthClockPort authClockPort;

  public ValidateTokenUseCase(
      AuthSessionPort authSessionPort,
      AuthUsersPort authUsersPort,
      AuthRbacPort authRbacPort,
      AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authUsersPort = authUsersPort;
    this.authRbacPort = authRbacPort;
    this.authClockPort = authClockPort;
  }

  @Transactional(readOnly = true)
  public Optional<TokenPrincipal> execute(String rawAccessToken) {
    var now = authClockPort.now();
    return authSessionPort
        .findActiveUserId(rawAccessToken, now)
        .flatMap(userId -> authUsersPort.findById(userId).filter(u -> u.active()))
        .map(
            user -> {
              Set<String> permissions = authRbacPort.resolvePermissions(user.roles());
              return new TokenPrincipal(user.userId(), user.email(), user.roles(), permissions);
            });
  }
}
