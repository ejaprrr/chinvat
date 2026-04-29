package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenGeneratorPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenIssuerAdapter implements AuthTokenIssuerPort {
  private final AuthTokenGeneratorPort tokenGeneratorPort;

  @Value("${auth.tokens.access-ttl:PT15M}")
  private Duration accessTokenTtl;

  @Value("${auth.tokens.refresh-ttl:P14D}")
  private Duration refreshTokenTtl;

  public AuthTokenIssuerAdapter(AuthTokenGeneratorPort tokenGeneratorPort) {
    this.tokenGeneratorPort = tokenGeneratorPort;
  }

  @Override
  public IssuedTokenPair issue(Long userId, String email, Instant issuedAt) {
    Instant accessExpiresAt = issuedAt.plus(accessTokenTtl);
    Instant refreshExpiresAt = issuedAt.plus(refreshTokenTtl);

    String accessToken =
        tokenGeneratorPort.generateToken(
            userId, email, accessExpiresAt, eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind.ACCESS);
    String refreshToken =
        tokenGeneratorPort.generateToken(
            userId, email, refreshExpiresAt, eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind.REFRESH);

    return new IssuedTokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
  }
}
