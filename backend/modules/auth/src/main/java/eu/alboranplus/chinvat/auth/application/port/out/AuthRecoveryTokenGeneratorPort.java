package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Generates opaque password reset tokens. */
public interface AuthRecoveryTokenGeneratorPort {
  String generateToken(UUID userId, String email, Instant expiresAt);
}

