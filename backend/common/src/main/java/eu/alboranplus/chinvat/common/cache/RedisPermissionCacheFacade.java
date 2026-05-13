package eu.alboranplus.chinvat.common.cache;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPermissionCacheFacade implements PermissionCacheFacade {

  private final StringRedisTemplate stringRedisTemplate;
  private final Duration ttl;
  private final String keyPrefix;

  public RedisPermissionCacheFacade(
      StringRedisTemplate stringRedisTemplate,
      @Value("${app.security.permission-cache.ttl:PT15M}") Duration ttl,
      @Value("${app.security.permission-cache.key-prefix:chinvat:permissions:}")
          String keyPrefix) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.ttl = ttl;
    this.keyPrefix = keyPrefix;
  }

  @Override
  public Optional<Set<String>> findUserPermissions(UUID userId) {
    Set<String> cached = stringRedisTemplate.opsForSet().members(userPermissionsKey(userId));
    if (cached == null || cached.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(Set.copyOf(cached));
  }

  @Override
  public void cacheUserPermissions(UUID userId, Set<String> permissions) {
    String key = userPermissionsKey(userId);
    stringRedisTemplate.delete(key);
    if (permissions.isEmpty()) {
      return;
    }
    stringRedisTemplate.opsForSet().add(key, permissions.toArray(String[]::new));
    stringRedisTemplate.expire(key, ttl);
  }

  @Override
  public void evictUserPermissions(UUID userId) {
    stringRedisTemplate.delete(userPermissionsKey(userId));
  }

  @Override
  public void evictAllUserPermissions() {
    Set<String> keys = scanKeys(keyPrefix + "user:*:permissions");
    if (!keys.isEmpty()) {
      stringRedisTemplate.delete(keys);
    }
  }

  private Set<String> scanKeys(String pattern) {
    return stringRedisTemplate.execute(
        (RedisConnection connection) -> {
          Set<String> keys = new LinkedHashSet<>();
          ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
          try (Cursor<byte[]> cursor = connection.scan(options)) {
            while (cursor.hasNext()) {
              keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
          }
          return keys;
        });
  }

  private String userPermissionsKey(UUID userId) {
    return keyPrefix + "user:" + userId + ":permissions";
  }
}