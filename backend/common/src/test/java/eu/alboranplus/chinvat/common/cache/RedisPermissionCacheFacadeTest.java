package eu.alboranplus.chinvat.common.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class RedisPermissionCacheFacadeTest {

  private static final String KEY_PREFIX = "chinvat:permissions:";
  private static final Duration TTL = Duration.ofMinutes(15);
  private static final Long TEST_USER_ID = 42L;

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private SetOperations<String, String> setOperations;

  private RedisPermissionCacheFacade sut;

  @BeforeEach
  void setUp() {
    sut = new RedisPermissionCacheFacade(stringRedisTemplate, TTL, KEY_PREFIX);
  }

  @Test
  void findUserPermissions_cacheHit_returnsCachedPermissions() {
    String key = KEY_PREFIX + "user:" + TEST_USER_ID + ":permissions";
    Set<String> cachedPerms = Set.of("PROFILE:READ", "PROFILE:UPDATE");
    given(stringRedisTemplate.opsForSet()).willReturn(setOperations);
    given(setOperations.members(key)).willReturn(cachedPerms);

    Optional<Set<String>> result = sut.findUserPermissions(TEST_USER_ID);

    assertThat(result).isPresent().contains(cachedPerms);
  }

  @Test
  void findUserPermissions_cacheMiss_returnsEmpty() {
    String key = KEY_PREFIX + "user:" + TEST_USER_ID + ":permissions";
    given(stringRedisTemplate.opsForSet()).willReturn(setOperations);
    given(setOperations.members(key)).willReturn(Set.of());

    Optional<Set<String>> result = sut.findUserPermissions(TEST_USER_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void cacheUserPermissions_storesPermissionsWithTtl() {
    String key = KEY_PREFIX + "user:" + TEST_USER_ID + ":permissions";
    Set<String> perms = Set.of("RBAC:MANAGE", "USERS:MANAGE");
    given(stringRedisTemplate.opsForSet()).willReturn(setOperations);

    sut.cacheUserPermissions(TEST_USER_ID, perms);

    verify(stringRedisTemplate).delete(key);
    verify(setOperations).add(key, perms.toArray(String[]::new));
    verify(stringRedisTemplate).expire(key, TTL);
  }

  @Test
  void cacheUserPermissions_emptySet_deletesKey() {
    String key = KEY_PREFIX + "user:" + TEST_USER_ID + ":permissions";

    sut.cacheUserPermissions(TEST_USER_ID, Set.of());

    verify(stringRedisTemplate).delete(key);
  }

  @Test
  void evictUserPermissions_deletesUserKey() {
    String key = KEY_PREFIX + "user:" + TEST_USER_ID + ":permissions";

    sut.evictUserPermissions(TEST_USER_ID);

    verify(stringRedisTemplate).delete(key);
  }

  @Test
  void evictAllUserPermissions_scansAndDeletesAllPermissionKeys() {
    // This is an integration test for SCAN operation
    // In unit test, we verify the delete is called with the scanned keys
    String pattern = KEY_PREFIX + "user:*:permissions";
    Set<String> keysToDelete =
        Set.of(
            KEY_PREFIX + "user:1:permissions",
            KEY_PREFIX + "user:2:permissions",
            KEY_PREFIX + "user:3:permissions");

    given(stringRedisTemplate.execute((RedisCallback<Set<String>>) any()))
        .willReturn(keysToDelete);

    sut.evictAllUserPermissions();

    verify(stringRedisTemplate).delete(keysToDelete);
  }
}
