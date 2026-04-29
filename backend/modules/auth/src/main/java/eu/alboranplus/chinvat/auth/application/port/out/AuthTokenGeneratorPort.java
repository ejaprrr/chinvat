package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;

/** Token generator strategy. Default implementation uses opaque session tokens. */
public interface AuthTokenGeneratorPort {
  String generateToken(Long userId, String email, Instant expiresAt, AuthSessionTokenKind kind);
}

