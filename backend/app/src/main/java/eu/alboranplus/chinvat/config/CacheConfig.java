package eu.alboranplus.chinvat.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

  public static final String USERS_BY_ID_CACHE = "users.by-id";
  public static final String USERS_ALL_CACHE = "users.all";
  public static final String TRUST_CERTIFICATE_VALIDATION_CACHE = "trust.certificate-validation";
  public static final String TRUST_CREDENTIALS_BY_USER_CACHE = "trust.credentials-by-user";
  public static final String EIDAS_PROVIDERS_CACHE = "eidas.providers";

  /**
   * Treats cache errors as cache misses so stale/incompatible Redis entries (e.g. produced by a
   * previous serialization format) never propagate as 5xx errors. Spring will fall through to the
   * underlying method and repopulate the cache with the current format.
   */
  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        logger.warn("Cache GET error on '{}' key='{}' — treating as miss: {}", cache.getName(), key, e.getMessage());
        try {
          cache.evict(key);
        } catch (RuntimeException evictEx) {
          logger.debug("Could not evict corrupted cache entry: {}", evictEx.getMessage());
        }
      }

      @Override
      public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        logger.warn("Cache PUT error on '{}' key='{}': {}", cache.getName(), key, e.getMessage());
      }

      @Override
      public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        logger.warn("Cache EVICT error on '{}' key='{}': {}", cache.getName(), key, e.getMessage());
      }

      @Override
      public void handleCacheClearError(RuntimeException e, Cache cache) {
        logger.warn("Cache CLEAR error on '{}': {}", cache.getName(), e.getMessage());
      }
    };
  }

  @Bean
  public RedisCacheManager redisCacheManager(
      @Qualifier("appCacheRedisConnectionFactory") RedisConnectionFactory connectionFactory,
      @Value("${app.cache.key-prefix:chinvat:cache:}") String cacheKeyPrefix,
      @Value("${app.cache.default-ttl:PT5M}") Duration defaultTtl,
      @Value("${app.cache.users-by-id-ttl:PT5M}") Duration usersByIdTtl,
      @Value("${app.cache.users-all-ttl:PT2M}") Duration usersAllTtl,
      @Value("${app.cache.trust-certificate-validation-ttl:PT15M}") Duration trustValidationTtl,
      @Value("${app.cache.trust-credentials-by-user-ttl:PT2M}") Duration trustCredentialsTtl,
      @Value("${app.cache.eidas-providers-ttl:PT30M}") Duration eidasProvidersTtl) {

    RedisCacheConfiguration baseConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .prefixCacheNameWith(cacheKeyPrefix)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    buildCacheSerializer()));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put(USERS_BY_ID_CACHE, baseConfig.entryTtl(usersByIdTtl));
    cacheConfigs.put(USERS_ALL_CACHE, baseConfig.entryTtl(usersAllTtl));
    cacheConfigs.put(TRUST_CERTIFICATE_VALIDATION_CACHE, baseConfig.entryTtl(trustValidationTtl));
    cacheConfigs.put(TRUST_CREDENTIALS_BY_USER_CACHE, baseConfig.entryTtl(trustCredentialsTtl));
    cacheConfigs.put(EIDAS_PROVIDERS_CACHE, baseConfig.entryTtl(eidasProvidersTtl));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(baseConfig.entryTtl(defaultTtl))
        .withInitialCacheConfigurations(cacheConfigs)
        .build();
  }

  /**
   * Builds a {@link Jackson2JsonRedisSerializer} backed by an explicit {@link ObjectMapper}
   * configured for stable, consistent JSON serialization across restarts and upgrades.
   *
   * <p>Uses {@code JsonTypeInfo.As.PROPERTY} (writes {@code "@class":"..."} inline) for objects.
   * For collections (List, Set, Map), Jackson automatically falls back to WRAPPER_ARRAY format
   * since JSON arrays cannot contain embedded properties. Allowed subtypes are restricted to our
   * own package tree plus JDK core types for defence-in-depth.
   */
  private static RedisSerializer<Object> buildCacheSerializer() {
    ObjectMapper mapper =
        new ObjectMapper()
            .findAndRegisterModules()
            .activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType("eu.alboranplus.chinvat")
                    .allowIfSubType("java.util")
                    .allowIfSubType("java.lang")
                    .allowIfSubType("java.time")
                    .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
    return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
  }
}
