package eu.alboranplus.chinvat.eidas.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.alboranplus.chinvat.eidas.infrastructure.config.EidasProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisEidasStateAdapterTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  private RedisEidasStateAdapter sut;

  @BeforeEach
  void setUp() {
    EidasProperties properties = new EidasProperties();
    properties.setStateKeyPrefix("chinvat:eidas:state:");
    sut = new RedisEidasStateAdapter(stringRedisTemplate, properties);
  }

  @Test
  void save_storesPayloadAndTtl() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    Instant expiresAt = Instant.now().plusSeconds(600);

    sut.save("state-1", "EIDAS_EU", expiresAt);

    then(valueOperations).should().set(eq("chinvat:eidas:state:state-1"), eq("EIDAS_EU|" + expiresAt.toEpochMilli()));
    then(stringRedisTemplate).should().expire(eq("chinvat:eidas:state:state-1"), any(Duration.class));
  }

  @Test
  void consume_returnsAndDeletesState() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    Instant expiresAt = Instant.now().plusSeconds(600);
    given(valueOperations.get("chinvat:eidas:state:state-1"))
        .willReturn("EIDAS_EU|" + expiresAt.toEpochMilli());

    var result = sut.consume("state-1");

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().providerCode()).isEqualTo("EIDAS_EU");
    then(stringRedisTemplate).should().delete("chinvat:eidas:state:state-1");
  }

  @Test
  void find_returnsEmptyWhenPayloadMissing() {
    given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get("chinvat:eidas:state:state-1")).willReturn(null);

    assertThat(sut.find("state-1")).isEmpty();
  }
}
