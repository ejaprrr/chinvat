package eu.alboranplus.chinvat.auth.infrastructure.crypto;

import eu.alboranplus.chinvat.auth.application.port.out.AuthRecoveryTokenGeneratorPort;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecoveryOpaqueTokenGeneratorAdapter implements AuthRecoveryTokenGeneratorPort {

  private static final int RESET_CODE_BOUND = 1_000_000;

  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generateToken(UUID userId, String email, Instant expiresAt) {
    int code = secureRandom.nextInt(RESET_CODE_BOUND);
    return String.format("%06d", code);
  }
}

