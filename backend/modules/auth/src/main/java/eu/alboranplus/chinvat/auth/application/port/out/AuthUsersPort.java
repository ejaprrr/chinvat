package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import java.time.Instant;
import java.util.Optional;

public interface AuthUsersPort {
  Optional<AuthUserProjection> findByEmail(String email);

  Optional<AuthUserProjection> findById(Long userId);

  Optional<AuthUserProjection> findByCertificateThumbprint(String thumbprintSha256, Instant now);

  boolean verifyPassword(String email, String rawPassword);
}
