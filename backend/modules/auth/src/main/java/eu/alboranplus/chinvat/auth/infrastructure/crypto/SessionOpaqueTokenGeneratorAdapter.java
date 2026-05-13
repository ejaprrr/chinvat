package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import eu.alboranplus.chinvat.auth.application.port.out.AuthTokenGeneratorPort;
import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SessionOpaqueTokenGeneratorAdapter implements AuthTokenGeneratorPort {

  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final int TOKEN_SIZE_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generateToken(
      UUID userId, String email, Instant expiresAt, AuthSessionTokenKind kind) {
    byte[] entropy = new byte[TOKEN_SIZE_BYTES];
    secureRandom.nextBytes(entropy);

    String payload =
        kind.tokenPrefix()
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

