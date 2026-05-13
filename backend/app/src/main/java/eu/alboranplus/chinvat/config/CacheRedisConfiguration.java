package eu.alboranplus.chinvat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  private static final Logger logger = LoggerFactory.getLogger(CacheRedisConfiguration.class);

  @Bean(name = "appCacheRedisConnectionFactory")
  public RedisConnectionFactory appCacheRedisConnectionFactory(
      @Value("${app.cache.redis.dedicated:false}") boolean dedicated,
      @Value("${app.cache.redis.host:}") String host,
      @Value("${app.cache.redis.port:6379}") int port,
      @Value("${app.cache.redis.database:2}") int database,
      @Value("${app.cache.redis.username:}") String username,
      @Value("${app.cache.redis.password:}") String password,
      @Value("${spring.data.redis.host:localhost}") String sharedHost,
      @Value("${spring.data.redis.port:6379}") int sharedPort,
      @Value("${spring.data.redis.database:0}") int sharedDatabase,
      @Value("${spring.data.redis.username:}") String sharedUsername,
      @Value("${spring.data.redis.password:}") String sharedPassword,
      @Autowired(required = false) @Qualifier("redisConnectionFactory")
          RedisConnectionFactory sharedConnectionFactory) {
    if (!dedicated) {
      if (sharedConnectionFactory == null) {
        logger.warn(
            "Shared redisConnectionFactory bean is missing, creating fallback Redis connection factory from spring.data.redis properties");
        return createConnectionFactory(
            sharedHost, sharedPort, sharedDatabase, sharedUsername, sharedPassword);
      }
      return sharedConnectionFactory;
    }

    return createConnectionFactory(host, port, database, username, password);
  }

  private static RedisConnectionFactory createConnectionFactory(
      String host, int port, int database, String username, String password) {
    RedisStandaloneConfiguration standaloneConfiguration =
        new RedisStandaloneConfiguration(host, port);
    standaloneConfiguration.setDatabase(database);

    if (isNotBlank(username)) {
      standaloneConfiguration.setUsername(username);
    }
    if (isNotBlank(password)) {
      standaloneConfiguration.setPassword(RedisPassword.of(password));
    }

    LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(standaloneConfiguration);
    connectionFactory.afterPropertiesSet();
    return connectionFactory;
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }
}
