package eu.alboranplus.chinvat.eidas.infrastructure.adapter;

import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import eu.alboranplus.chinvat.eidas.infrastructure.config.EidasProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisEidasStateAdapter implements EidasStatePort {

  private final StringRedisTemplate stringRedisTemplate;
  private final EidasProperties eidasProperties;

  public RedisEidasStateAdapter(
      @Qualifier("eidasStateRedisTemplate") StringRedisTemplate stringRedisTemplate,
      EidasProperties eidasProperties) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.eidasProperties = eidasProperties;
  }

  @Override
  public void save(String state, String providerCode, Instant expiresAt) {
    String payload = providerCode + "|" + expiresAt.toEpochMilli();
    String key = key(state);
    stringRedisTemplate.opsForValue().set(key, payload);
    Duration ttl = Duration.between(Instant.now(), expiresAt);
    if (!ttl.isNegative() && !ttl.isZero()) {
      stringRedisTemplate.expire(key, ttl);
    } else {
      stringRedisTemplate.delete(key);
    }
  }

  @Override
  public Optional<EidasStateRecord> consume(String state) {
    String key = key(state);
    String payload = stringRedisTemplate.opsForValue().get(key);
    stringRedisTemplate.delete(key);
    return parse(state, payload);
  }

  @Override
  public Optional<EidasStateRecord> find(String state) {
    return parse(state, stringRedisTemplate.opsForValue().get(key(state)));
  }

  private Optional<EidasStateRecord> parse(String state, String payload) {
    if (payload == null || payload.isBlank()) {
      return Optional.empty();
    }
    String[] parts = payload.split("\\|", 2);
    if (parts.length != 2) {
      return Optional.empty();
    }
    Instant expiresAt = Instant.ofEpochMilli(Long.parseLong(parts[1]));
    return Optional.of(new EidasStateRecord(state, parts[0], expiresAt));
  }

  private String key(String state) {
    return eidasProperties.getStateKeyPrefix() + state;
  }
}