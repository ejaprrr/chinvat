package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.CertificateLoginCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.application.service.AuthPermissionService;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateLoginUseCase {

  private final AuthUsersPort authUsersPort;
  private final AuthPermissionService authPermissionService;
  private final AuthTokenIssuerPort authTokenIssuerPort;
  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public CertificateLoginUseCase(
      AuthUsersPort authUsersPort,
      AuthPermissionService authPermissionService,
      AuthTokenIssuerPort authTokenIssuerPort,
      AuthSessionPort authSessionPort,
      AuthClockPort authClockPort) {
    this.authUsersPort = authUsersPort;
    this.authPermissionService = authPermissionService;
    this.authTokenIssuerPort = authTokenIssuerPort;
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public AuthResult execute(CertificateLoginCommand command) {
    Instant now = authClockPort.now();
    AuthUserProjection user =
        authUsersPort
            .findByCertificateThumbprint(command.thumbprintSha256(), now)
            .filter(AuthUserProjection::active)
            .orElseThrow(
                () -> new InvalidAuthenticationException("No active user is registered for this certificate"));

    var permissions = authPermissionService.resolvePermissions(user.userId(), user.roles());
    IssuedTokenPair tokens = authTokenIssuerPort.issue(user.userId(), user.email(), now);

    authSessionPort.save(
        user.userId(),
        AuthSessionTokenKind.ACCESS,
        tokens.accessToken(),
        now,
        tokens.expiresAt(),
        command.clientIp(),
        command.userAgent());

    authSessionPort.save(
        user.userId(),
        AuthSessionTokenKind.REFRESH,
        tokens.refreshToken(),
        now,
        tokens.refreshExpiresAt(),
        command.clientIp(),
        command.userAgent());

    return new AuthResult(
        user.userId(), user.email(), user.displayName(), user.roles(), permissions, tokens);
  }
}