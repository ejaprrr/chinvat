package eu.alboranplus.chinvat.users.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UsersPasswordPort {
  Optional<String> findHashByUserId(UUID userId);

  void save(UUID userId, String passwordHash, String algorithm);
}
