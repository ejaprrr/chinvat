package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthRbacPort authRbacPort;
  private final AuthTokenIssuerPort authTokenIssuerPort;
  private final AuthClockPort authClockPort;

  public LoginUseCase(
      AuthUsersPort authUsersPort,
      AuthRbacPort authRbacPort,
      AuthTokenIssuerPort authTokenIssuerPort,
      AuthClockPort authClockPort) {
    this.authUsersPort = authUsersPort;
    this.authRbacPort = authRbacPort;
    this.authTokenIssuerPort = authTokenIssuerPort;
    this.authClockPort = authClockPort;
  }

  public AuthResult execute(LoginCommand command) {
    AuthUserProjection user =
        authUsersPort
            .findByEmail(command.email())
            .filter(AuthUserProjection::active)
            .orElseThrow(() -> new InvalidAuthenticationException("Invalid email or password"));

    boolean valid = authUsersPort.verifyPassword(command.email(), command.password());
    if (!valid) {
      throw new InvalidAuthenticationException("Invalid email or password");
    }

    Set<String> permissions = authRbacPort.resolvePermissions(user.roles());
    Instant now = authClockPort.now();
    IssuedTokenPair tokens = authTokenIssuerPort.issue(user.userId(), user.email(), now);

    return new AuthResult(
        user.userId(), user.email(), user.displayName(), user.roles(), permissions, tokens);
  }
}
