package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthRbacPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthUsersPort;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthUsersPort authUsersPort;
  private final AuthRbacPort authRbacPort;
  private final AuthTokenIssuerPort authTokenIssuerPort;
  private final AuthClockPort authClockPort;

  public RefreshTokenUseCase(
      AuthSessionPort authSessionPort,
      AuthUsersPort authUsersPort,
      AuthRbacPort authRbacPort,
      AuthTokenIssuerPort authTokenIssuerPort,
      AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authUsersPort = authUsersPort;
    this.authRbacPort = authRbacPort;
    this.authTokenIssuerPort = authTokenIssuerPort;
    this.authClockPort = authClockPort;
  }

  @Transactional
  public AuthResult execute(RefreshCommand command) {
    Instant now = authClockPort.now();

    Long userId =
        authSessionPort
            .findActiveUserId(command.refreshToken(), now)
            .orElseThrow(() -> new InvalidAuthenticationException("Invalid or expired refresh token"));

    AuthUserProjection user =
        authUsersPort
            .findById(userId)
            .filter(AuthUserProjection::active)
            .orElseThrow(() -> new InvalidAuthenticationException("User not found or inactive"));

    // Refresh token rotation — revoke the used refresh token
    authSessionPort.revokeByRawToken(command.refreshToken(), now);

    Set<String> permissions = authRbacPort.resolvePermissions(user.roles());
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
