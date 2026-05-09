package eu.alboranplus.chinvat.eidas.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

class EidasStateRedisConfigurationTest {

  private final EidasStateRedisConfiguration configuration = new EidasStateRedisConfiguration();

  @Test
  void eidasStateRedisTemplate_whenDedicatedDisabled_reusesSharedTemplate() {
    EidasProperties eidasProperties = new EidasProperties();
    eidasProperties.getStateRedis().setDedicated(false);

    StringRedisTemplate sharedTemplate = new StringRedisTemplate();

    StringRedisTemplate result =
      configuration.eidasStateRedisTemplate(eidasProperties, sharedTemplate);

    assertThat(result).isSameAs(sharedTemplate);
  }

  @Test
  void eidasStateRedisTemplate_whenDedicatedEnabled_createsDedicatedTemplate() {
    EidasProperties eidasProperties = new EidasProperties();
    eidasProperties.getStateRedis().setDedicated(true);
    eidasProperties.getStateRedis().setHost("dedicated-redis");
    eidasProperties.getStateRedis().setPort(6380);
    eidasProperties.getStateRedis().setDatabase(3);

    StringRedisTemplate sharedTemplate = new StringRedisTemplate();

    StringRedisTemplate result =
      configuration.eidasStateRedisTemplate(eidasProperties, sharedTemplate);

    assertThat(result).isNotSameAs(sharedTemplate);
    assertThat(result.getConnectionFactory()).isNotNull();
  }
}
