package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMeUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthRbacPort authRbacPort;

  public GetMeUseCase(AuthUsersPort authUsersPort, AuthRbacPort authRbacPort) {
    this.authUsersPort = authUsersPort;
    this.authRbacPort = authRbacPort;
  }

  @Transactional(readOnly = true)
  public AuthMeView execute(TokenPrincipal principal) {
    AuthUserProjection user =
        authUsersPort
            .findById(principal.userId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

    var permissions = authRbacPort.resolvePermissions(user.roles());

    return new AuthMeView(
        user.userId(), user.email(), user.displayName(), user.roles(), permissions);
  }
}

