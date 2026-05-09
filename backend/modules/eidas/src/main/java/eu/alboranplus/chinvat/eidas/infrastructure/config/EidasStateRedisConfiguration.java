package eu.alboranplus.chinvat.eidas.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class EidasStateRedisConfiguration {

  @Bean(name = "eidasStateRedisTemplate")
  public StringRedisTemplate eidasStateRedisTemplate(
      EidasProperties eidasProperties,
      @Qualifier("stringRedisTemplate") StringRedisTemplate sharedRedisTemplate) {
    if (!eidasProperties.getStateRedis().isDedicated()) {
      return sharedRedisTemplate;
    }

    RedisStandaloneConfiguration standaloneConfiguration =
        new RedisStandaloneConfiguration(
        eidasProperties.getStateRedis().getHost(),
            eidasProperties.getStateRedis().getPort());

    standaloneConfiguration.setDatabase(eidasProperties.getStateRedis().getDatabase());

    if (isNotBlank(eidasProperties.getStateRedis().getUsername())) {
      standaloneConfiguration.setUsername(eidasProperties.getStateRedis().getUsername());
    }
    if (isNotBlank(eidasProperties.getStateRedis().getPassword())) {
      standaloneConfiguration.setPassword(
          RedisPassword.of(eidasProperties.getStateRedis().getPassword()));
    }

    RedisConnectionFactory connectionFactory =
        new LettuceConnectionFactory(standaloneConfiguration);
    ((LettuceConnectionFactory) connectionFactory).afterPropertiesSet();

    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);
    template.afterPropertiesSet();
    return template;
  }

  private static boolean isNotBlank(String value) {
    return value != null && !value.isBlank();
  }
}
