package eu.alboranplus.chinvat.users.application.port.out;

import java.util.Optional;

public interface UsersPasswordPort {
  Optional<String> findHashByUserId(Long userId);

  void save(Long userId, String passwordHash, String algorithm);
}
