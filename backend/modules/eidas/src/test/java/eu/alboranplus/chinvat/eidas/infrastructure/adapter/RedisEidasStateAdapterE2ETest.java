package eu.alboranplus.chinvat.eidas.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.eidas.infrastructure.config.EidasProperties;
import java.time.Instant;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.DockerClientFactory;

class RedisEidasStateAdapterE2ETest {

  private static GenericContainer<?> redis;

  private static LettuceConnectionFactory sharedConnectionFactory;
  private static StringRedisTemplate sharedStringRedisTemplate;
  private static LettuceConnectionFactory dedicatedConnectionFactory;
  private static StringRedisTemplate dedicatedStringRedisTemplate;

  @BeforeAll
  static void setUpRedisClient() {
    Assumptions.assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is not available, skipping Redis E2E test");

    redis = new GenericContainer<>("redis:7.2-alpine").withExposedPorts(6379);
    redis.start();

    sharedConnectionFactory =
        new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
    sharedConnectionFactory.setDatabase(0);
    sharedConnectionFactory.afterPropertiesSet();
    sharedStringRedisTemplate = new StringRedisTemplate(sharedConnectionFactory);
    sharedStringRedisTemplate.afterPropertiesSet();

    dedicatedConnectionFactory =
      new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
    dedicatedConnectionFactory.setDatabase(1);
    dedicatedConnectionFactory.afterPropertiesSet();
    dedicatedStringRedisTemplate = new StringRedisTemplate(dedicatedConnectionFactory);
    dedicatedStringRedisTemplate.afterPropertiesSet();
  }

  @AfterAll
  static void tearDownRedisClient() {
    if (dedicatedConnectionFactory != null) {
      dedicatedConnectionFactory.destroy();
    }
    if (sharedConnectionFactory != null) {
      sharedConnectionFactory.destroy();
    }
    if (redis != null) {
      redis.stop();
    }
  }

  @Test
  void saveAndConsume_worksAgainstRealRedis() {
    EidasProperties properties = new EidasProperties();
    properties.setStateKeyPrefix("chinvat:eidas:e2e:");

    RedisEidasStateAdapter adapter = new RedisEidasStateAdapter(dedicatedStringRedisTemplate, properties);
    Instant expiresAt = Instant.now().plusSeconds(120);

    adapter.save("state-e2e-1", "EIDAS_EU", expiresAt);

    var found = adapter.find("state-e2e-1");
    assertThat(found).isPresent();
    assertThat(found.orElseThrow().providerCode()).isEqualTo("EIDAS_EU");

    var consumed = adapter.consume("state-e2e-1");
    assertThat(consumed).isPresent();
    assertThat(adapter.find("state-e2e-1")).isEmpty();
  }

  @Test
  void stateStorage_canBeIsolatedUsingDedicatedRedisDatabaseIndex() {
    EidasProperties properties = new EidasProperties();
    properties.setStateKeyPrefix("chinvat:eidas:isolated:");

    RedisEidasStateAdapter adapter = new RedisEidasStateAdapter(dedicatedStringRedisTemplate, properties);
    sharedStringRedisTemplate.opsForValue().set("chinvat:permissions:user:77", "READ,WRITE");

    adapter.save("state-isolated-1", "EIDAS_EU", Instant.now().plusSeconds(120));

    assertThat(sharedStringRedisTemplate.opsForValue().get("chinvat:eidas:isolated:state-isolated-1"))
        .isNull();
    assertThat(dedicatedStringRedisTemplate.opsForValue().get("chinvat:permissions:user:77"))
        .isNull();
    assertThat(adapter.find("state-isolated-1")).isPresent();
  }
}
