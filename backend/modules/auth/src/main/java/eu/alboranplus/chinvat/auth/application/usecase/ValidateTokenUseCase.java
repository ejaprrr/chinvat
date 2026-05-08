package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidateTokenUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthUsersPort authUsersPort;
  private final AuthPermissionService authPermissionService;
  private final AuthClockPort authClockPort;

  public ValidateTokenUseCase(
      AuthSessionPort authSessionPort,
      AuthUsersPort authUsersPort,
      AuthPermissionService authPermissionService,
      AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authUsersPort = authUsersPort;
    this.authPermissionService = authPermissionService;
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
              var permissions = authPermissionService.resolvePermissions(user.userId(), user.roles());
              return new TokenPrincipal(user.userId(), user.email(), user.roles(), permissions);
            });
  }
}
