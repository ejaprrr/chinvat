package eu.alboranplus.chinvat.common.cache;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PermissionCacheFacade {
  Optional<Set<String>> findUserPermissions(UUID userId);

  void cacheUserPermissions(UUID userId, Set<String> permissions);

  void evictUserPermissions(UUID userId);

  void evictAllUserPermissions();
}