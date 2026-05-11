package eu.alboranplus.chinvat.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed rate limiting store for distributed rate limiting across multiple instances.
 * 
 * Stores token bucket state in Redis with automatic expiration.
 * Ensures rate limits are enforced globally, not per-instance.
 */
@Component
public class RedisRateLimitingStore {

  private static final Logger logger = LoggerFactory.getLogger(RedisRateLimitingStore.class);
  private static final String KEY_PREFIX = "chinvat:ratelimit:";
  private static final String TOKENS_SUFFIX = ":tokens";
  private static final String LAST_REFILL_SUFFIX = ":last_refill";

  private final StringRedisTemplate redisTemplate;

  public RedisRateLimitingStore(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Get current token count for a rate limit key.
   * If key doesn't exist, returns Optional.empty() (bucket expired or new).
   */
  public Optional<Double> getTokens(String key) {
    try {
      String value = redisTemplate.opsForValue().get(KEY_PREFIX + key + TOKENS_SUFFIX);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.of(Double.parseDouble(value));
    } catch (Exception e) {
      logger.warn("Failed to get tokens from Redis for key: {}", key, e);
      return Optional.empty();
    }
  }

  /**
   * Get last refill timestamp (milliseconds since epoch).
   * If key doesn't exist, returns Optional.empty().
   */
  public Optional<Long> getLastRefill(String key) {
    try {
      String value = redisTemplate.opsForValue().get(KEY_PREFIX + key + LAST_REFILL_SUFFIX);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.of(Long.parseLong(value));
    } catch (Exception e) {
      logger.warn("Failed to get last refill from Redis for key: {}", key, e);
      return Optional.empty();
    }
  }

  /**
   * Update token count and last refill time in Redis.
   * Automatically expires after specified duration.
   */
  public void updateBucket(String key, double tokens, long lastRefillMs, Duration ttl) {
    try {
      String tokensKey = KEY_PREFIX + key + TOKENS_SUFFIX;
      String lastRefillKey = KEY_PREFIX + key + LAST_REFILL_SUFFIX;
      
      redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), ttl);
      redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(lastRefillMs), ttl);
    } catch (Exception e) {
      logger.warn("Failed to update bucket in Redis for key: {}", key, e);
    }
  }

  /**
   * Initialize a new bucket in Redis for a rate limit key.
   * Used when rate limit is first encountered.
   */
  public void initializeBucket(String key, double initialTokens, long nowMs, Duration ttl) {
    try {
      String tokensKey = KEY_PREFIX + key + TOKENS_SUFFIX;
      String lastRefillKey = KEY_PREFIX + key + LAST_REFILL_SUFFIX;
      
      redisTemplate.opsForValue().set(tokensKey, String.valueOf(initialTokens), ttl);
      redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(nowMs), ttl);
      
      logger.debug("Initialized rate limit bucket in Redis: key={}, tokens={}", key, initialTokens);
    } catch (Exception e) {
      logger.warn("Failed to initialize bucket in Redis for key: {}", key, e);
    }
  }

  /**
   * Delete a bucket from Redis (for testing or manual cleanup).
   */
  public void deleteBucket(String key) {
    try {
      redisTemplate.delete(KEY_PREFIX + key + TOKENS_SUFFIX);
      redisTemplate.delete(KEY_PREFIX + key + LAST_REFILL_SUFFIX);
    } catch (Exception e) {
      logger.warn("Failed to delete bucket from Redis for key: {}", key, e);
    }
  }

  /**
   * Get approximate number of active rate limit buckets in Redis.
   * Useful for monitoring.
   */
  public long getActiveBucketCount() {
    try {
      Long count = redisTemplate.keys(KEY_PREFIX + "*").stream().count();
      return count;
    } catch (Exception e) {
      logger.warn("Failed to get active bucket count from Redis", e);
      return 0L;
    }
  }
}
