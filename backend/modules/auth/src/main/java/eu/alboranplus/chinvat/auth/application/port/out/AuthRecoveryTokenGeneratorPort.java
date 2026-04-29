package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;

/** Generates opaque password reset tokens. */
public interface AuthRecoveryTokenGeneratorPort {
  String generateToken(Long userId, String email, Instant expiresAt);
}

