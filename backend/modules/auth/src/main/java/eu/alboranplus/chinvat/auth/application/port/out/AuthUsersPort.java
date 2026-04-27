package eu.alboranplus.chinvat.auth.application.port.out;

import eu.alboranplus.chinvat.auth.application.dto.AuthUserProjection;
import java.util.Optional;

public interface AuthUsersPort {
  Optional<AuthUserProjection> findByEmail(String email);

  Optional<AuthUserProjection> findById(Long userId);

  boolean verifyPassword(String email, String rawPassword);
}
