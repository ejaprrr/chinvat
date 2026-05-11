package eu.alboranplus.chinvat.users.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface UsersRoleRepositoryPort {
  Set<String> findRoleNamesByUserId(UUID userId);
}
