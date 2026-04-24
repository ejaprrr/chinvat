package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenIssuerPort;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenIssuerAdapter implements AuthTokenIssuerPort {

  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final int TOKEN_SIZE_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();

  @Value("${auth.tokens.access-ttl:PT15M}")
  private Duration accessTokenTtl;

  @Value("${auth.tokens.refresh-ttl:P14D}")
  private Duration refreshTokenTtl;

  @Override
  public IssuedTokenPair issue(Long userId, String email, Instant issuedAt) {
    Instant accessExpiresAt = issuedAt.plus(accessTokenTtl);

    String accessToken = newToken(userId, email, accessExpiresAt, "A");
    String refreshToken = newToken(userId, email, issuedAt.plus(refreshTokenTtl), "R");

    return new IssuedTokenPair(accessToken, refreshToken, accessExpiresAt);
  }

  private String newToken(Long userId, String email, Instant expiresAt, String tokenKind) {
    byte[] entropy = new byte[TOKEN_SIZE_BYTES];
    secureRandom.nextBytes(entropy);

    String payload =
        tokenKind
            + ":"
            + userId
            + ":"
            + email
            + ":"
            + expiresAt.toEpochMilli()
            + ":"
            + URL_ENCODER.encodeToString(entropy);

    return URL_ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
  }
}
