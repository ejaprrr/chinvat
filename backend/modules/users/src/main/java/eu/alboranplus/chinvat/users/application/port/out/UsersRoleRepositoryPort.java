package eu.alboranplus.chinvat.users.application.port.out;

import java.util.Set;

public interface UsersRoleRepositoryPort {
  Set<String> findRoleNamesByUserId(Long userId);
}
