package eu.alboranplus.chinvat.common.cache;

import java.util.Optional;
import java.util.Set;

public interface PermissionCacheFacade {
  Optional<Set<String>> findUserPermissions(Long userId);

  void cacheUserPermissions(Long userId, Set<String> permissions);

  void evictUserPermissions(Long userId);

  void evictAllUserPermissions();
}