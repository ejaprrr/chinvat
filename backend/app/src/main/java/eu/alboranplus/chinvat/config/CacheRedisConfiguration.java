package eu.alboranplus.chinvat.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class CacheRedisConfiguration {

  @Bean(name = "appCacheRedisConnectionFactory")
  public RedisConnectionFactory appCacheRedisConnectionFactory(
      @Value("${app.cache.redis.dedicated:false}") boolean dedicated,
      @Value("${app.cache.redis.host:}") String host,
      @Value("${app.cache.redis.port:6379}") int port,
      @Value("${app.cache.redis.database:2}") int database,
      @Value("${app.cache.redis.username:}") String username,
      @Value("${app.cache.redis.password:}") String password,
      @Qualifier("redisConnectionFactory") RedisConnectionFactory sharedConnectionFactory) {
    if (!dedicated) {
      return sharedConnectionFactory;
    }

    RedisStandaloneConfiguration standaloneConfiguration =
        new RedisStandaloneConfiguration(host, port);
    standaloneConfiguration.setDatabase(database);

    if (isNotBlank(username)) {
      standaloneConfiguration.setUsername(username);
    }
    if (isNotBlank(password)) {
      standaloneConfiguration.setPassword(RedisPassword.of(password));
    }

    LettuceConnectionFactory dedicatedConnectionFactory =
        new LettuceConnectionFactory(standaloneConfiguration);
    dedicatedConnectionFactory.afterPropertiesSet();
    return dedicatedConnectionFactory;
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }
}
