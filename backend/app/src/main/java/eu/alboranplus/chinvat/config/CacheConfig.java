package eu.alboranplus.chinvat.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String USERS_BY_ID_CACHE = "users.by-id";
  public static final String USERS_ALL_CACHE = "users.all";
  public static final String TRUST_CERTIFICATE_VALIDATION_CACHE = "trust.certificate-validation";
  public static final String TRUST_CREDENTIALS_BY_USER_CACHE = "trust.credentials-by-user";
  public static final String EIDAS_PROVIDERS_CACHE = "eidas.providers";

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
                    new GenericJackson2JsonRedisSerializer()));

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
}
