package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import java.time.Instant;
import java.util.UUID;

/** Token generator strategy. Default implementation uses opaque session tokens. */
public interface AuthTokenGeneratorPort {
  String generateToken(UUID userId, String email, Instant expiresAt, AuthSessionTokenKind kind);
}

