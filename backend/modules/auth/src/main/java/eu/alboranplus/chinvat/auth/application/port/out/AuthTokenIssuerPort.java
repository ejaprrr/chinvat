package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import java.time.Instant;

public interface AuthTokenIssuerPort {
  IssuedTokenPair issue(Long userId, String email, Instant issuedAt);
}
