package eu.alboranplus.chinvat.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Centrální konfigurace Redis pro všechny moduly aplikace.
 * Poskytuje StringRedisTemplate pro cache a session management.
 */
@Configuration
public class RedisConfiguration {

  /**
   * Vytváří centrálně konfigurovaný StringRedisTemplate pro všechny moduly.
   * StringRedisTemplate automaticky používá StringRedisSerializer.
   *
   * @param connectionFactory Redis connection factory z Spring Boot auto-konfiguraci
   * @return nakonfigurovaný StringRedisTemplate
   */
  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);
    template.afterPropertiesSet();
    return template;
  }
}
