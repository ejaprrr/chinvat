package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import eu.alboranplus.chinvat.auth.application.port.out.AuthRecoveryTokenGeneratorPort;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class RecoveryOpaqueTokenGeneratorAdapter implements AuthRecoveryTokenGeneratorPort {

  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final int TOKEN_SIZE_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generateToken(Long userId, String email, Instant expiresAt) {
    byte[] entropy = new byte[TOKEN_SIZE_BYTES];
    secureRandom.nextBytes(entropy);

    String payload =
        "P"
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

