package eu.alboranplus.chinvat.security;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorDetail;
import eu.alboranplus.chinvat.config.RateLimitingConfig;
import eu.alboranplus.chinvat.config.RateLimitingConfig.RateLimit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Distributed token bucket rate limiter filter.
 * 
 * Enforces per-user and per-IP rate limits on incoming requests using:
 * - Redis backend for shared state across multiple instances (production)
 * - Local fallback for standalone/testing scenarios
 * 
 * Rate limit data is stored in redis-global under chinvat:ratelimit:* keyspace.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
  private final Map<String, TokenBucket> localBuckets = new ConcurrentHashMap<>();
  private final RateLimitingConfig config;
  private final Optional<RedisRateLimitingStore> redisStore;
  private final ApiSecurityErrorWriter errorWriter;
  private volatile Instant lastCleanup = Instant.now();
  private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);
  private static final Duration BUCKET_EXPIRY = Duration.ofHours(1);

  public RateLimitingFilter(
      RateLimitingConfig config,
      Optional<RedisRateLimitingStore> redisStore,
      ApiSecurityErrorWriter errorWriter) {
    this.config = config;
    this.redisStore = redisStore;
    this.errorWriter = errorWriter;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    if (isOperationalEndpoint(path)) {
      chain.doFilter(request, response);
      return;
    }

    Optional<String> limitType = resolveLimitType(path, method);
    if (limitType.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }

    String key = resolveBucketKey(request, limitType.get());
    RateLimit limit = getLimit(request, limitType.get());
    cleanupExpiredBuckets();

    TokenBucket bucket = getOrCreateBucket(key, limit);

    if (bucket.tryConsume()) {
      // Persist bucket state to Redis (if available) for distributed rate limiting
      persistBucketState(key, bucket);
      
      response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getRemaining()));
      response.addHeader("X-Rate-Limit-Reset-After-Millis", String.valueOf(bucket.getResetMs()));
      logger.debug("Rate limit OK: {}", path);
      chain.doFilter(request, response);
    } else {
      long waitSecs = bucket.getResetMs() / 1000;
      response.addHeader("Retry-After", String.valueOf(waitSecs));
      response.addHeader("X-Rate-Limit-Reset-After-Millis", String.valueOf(bucket.getResetMs()));
      errorWriter.write(
          response,
          HttpStatus.TOO_MANY_REQUESTS,
          ApiErrorCode.COMMON_RATE_LIMIT_EXCEEDED,
          "Rate limit exceeded",
          request.getRequestURI(),
          List.of(new ApiErrorDetail("retryAfterSeconds", "Retry after this amount of seconds", String.valueOf(waitSecs))));
    }
  }

  private TokenBucket getOrCreateBucket(String key, RateLimit limit) {
    // First, try to load from Redis (for distributed state)
    if (redisStore.isPresent()) {
      Optional<Double> redisTokens = redisStore.get().getTokens(key);
      Optional<Long> redisLastRefill = redisStore.get().getLastRefill(key);
      
      if (redisTokens.isPresent() && redisLastRefill.isPresent()) {
        // Bucket exists in Redis - restore state
        TokenBucket bucket = new TokenBucket(limit);
        bucket.restoreState(redisTokens.get(), redisLastRefill.get());
        localBuckets.put(key, bucket);
        return bucket;
      }
    }
    
    // Fall back to local cache or create new
    return localBuckets.computeIfAbsent(key, k -> {
      TokenBucket bucket = new TokenBucket(limit);
      // Initialize in Redis if available
      if (redisStore.isPresent()) {
        redisStore.get().initializeBucket(k, limit.capacity(), System.currentTimeMillis(), 
            BUCKET_EXPIRY);
      }
      return bucket;
    });
  }

  private void persistBucketState(String key, TokenBucket bucket) {
    if (redisStore.isPresent()) {
      redisStore.get().updateBucket(key, bucket.getTokens(), 
          bucket.getLastRefillMs(), BUCKET_EXPIRY);
    }
  }

  private String resolveBucketKey(HttpServletRequest request, String type) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      String userId = extractUserId(auth);
      if (userId != null) {
        return type + ":user:" + userId;
      }
    }
    return type + ":ip:" + getClientIp(request);
  }

  private RateLimit getLimit(HttpServletRequest request, String type) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      return config.getUserLimit(type);
    }
    return config.getIpLimit(type);
  }

  private boolean isOperationalEndpoint(String path) {
    return path.startsWith("/actuator") || path.startsWith("/health") || path.contains("/health") ||
        path.contains("/swagger") || path.contains("/v3/api-docs") ||
        path.contains("/h2-console");
  }

  private Optional<String> resolveLimitType(String path, String method) {
    if (path.contains("/login")) return Optional.of("LOGIN");
    if (path.contains("/register")) return Optional.of("REGISTER");
    if (path.contains("/password-reset") || path.contains("/password/reset"))
      return Optional.of("PASSWORD_RESET");
    if (path.contains("/certificate") && "POST".equals(method))
      return Optional.of("CERTIFICATE_BIND");
    if (path.contains("/eidas") || path.contains("/eid"))
      return Optional.of("EIDAS_LOGIN");
    return Optional.of("API_GENERAL");
  }

  private String extractUserId(Authentication auth) {
    try {
      Object principal = auth.getPrincipal();
      if (principal instanceof TokenPrincipal tp) {
        return tp.userId().toString();
      }
    } catch (Exception e) {
      logger.debug("Cannot extract user ID", e);
    }
    return null;
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwarded = request.getHeader("X-Forwarded-For");
    if (xForwarded != null && !xForwarded.isEmpty()) {
      return xForwarded.split(",")[0].trim();
    }
    String xReal = request.getHeader("X-Real-IP");
    if (xReal != null && !xReal.isEmpty()) {
      return xReal;
    }
    return request.getRemoteAddr();
  }

  private void cleanupExpiredBuckets() {
    Instant now = Instant.now();
    if (Duration.between(lastCleanup, now).compareTo(CLEANUP_INTERVAL) > 0) {
      localBuckets.entrySet().removeIf(e -> Duration.between(e.getValue().getCreatedAt(), now)
          .compareTo(BUCKET_EXPIRY) > 0);
      lastCleanup = now;
      logger.debug("Cleaned up expired local buckets. Remaining: {}", localBuckets.size());
    }
  }

  private static class TokenBucket {
    private final int capacity;
    private final Duration window;
    private double tokens;
    private long lastRefillMs;
    private Instant createdAt;

    TokenBucket(RateLimit limit) {
      this.capacity = limit.capacity();
      this.window = limit.refillDuration();
      this.tokens = capacity;
      this.lastRefillMs = System.currentTimeMillis();
      this.createdAt = Instant.now();
    }

    synchronized boolean tryConsume() {
      refill();
      if (tokens >= 1.0) {
        tokens -= 1.0;
        return true;
      }
      return false;
    }

    synchronized long getRemaining() {
      refill();
      return (long) tokens;
    }

    synchronized long getResetMs() {
      refill();
      long elapsed = System.currentTimeMillis() - lastRefillMs;
      long windowMs = window.toMillis();
      return Math.max(0, windowMs - elapsed);
    }

    synchronized double getTokens() {
      refill();
      return tokens;
    }

    synchronized long getLastRefillMs() {
      return lastRefillMs;
    }

    synchronized Instant getCreatedAt() {
      return createdAt;
    }

    synchronized void restoreState(double restoredTokens, long restoredLastRefillMs) {
      this.tokens = restoredTokens;
      this.lastRefillMs = restoredLastRefillMs;
    }

    private void refill() {
      long nowMs = System.currentTimeMillis();
      long elapsed = nowMs - lastRefillMs;
      long windowMs = window.toMillis();

      if (elapsed >= windowMs) {
        tokens = capacity;
        lastRefillMs = nowMs;
      } else {
        double tokensToAdd = (double) elapsed / windowMs * capacity;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillMs = nowMs;
      }
    }
  }
}
