package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import java.time.Instant;
import java.util.UUID;

public interface AuthTokenIssuerPort {
  IssuedTokenPair issue(UUID userId, String email, Instant issuedAt);
}
