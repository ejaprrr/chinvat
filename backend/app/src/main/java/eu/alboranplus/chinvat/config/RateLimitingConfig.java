package eu.alboranplus.chinvat.config;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;

/**
 * Rate limiting configuration.
 * Provides limit values for different endpoint types and authentication states.
 * 
 * For production, consider using bucket4j-redis for distributed rate limiting
 * across multiple application instances.
 */
@Configuration
public class RateLimitingConfig {

  /**
   * Get rate limit for authenticated users by endpoint type.
   * Returns: {capacity, refillDuration}
   */
  public RateLimit getUserLimit(String limitType) {
    return switch (limitType) {
      case "LOGIN" -> 
        // 10 login attempts per 5 minutes (brute force protection)
        new RateLimit(10, Duration.ofMinutes(5));
      case "PASSWORD_RESET" -> 
        // 3 password reset requests per hour (account takeover prevention)
        new RateLimit(3, Duration.ofHours(1));
      case "CERTIFICATE_BIND" -> 
        // 5 certificate bindings per hour (admin action audit)
        new RateLimit(5, Duration.ofHours(1));
      case "EIDAS_LOGIN" -> 
        // 10 eIDAS login attempts per hour
        new RateLimit(10, Duration.ofHours(1));
      case "API_GENERAL" -> 
        // 100 general API calls per minute (default for other endpoints)
        new RateLimit(100, Duration.ofMinutes(1));
      default -> 
        // Fallback: 50 requests per minute
        new RateLimit(50, Duration.ofMinutes(1));
    };
  }

  /**
   * Get rate limit for anonymous/IP-based access by endpoint type.
   */
  public RateLimit getIpLimit(String limitType) {
    return switch (limitType) {
      case "LOGIN" -> 
        // 5 login attempts per 5 minutes per IP (prevent brute force)
        new RateLimit(5, Duration.ofMinutes(5));
      case "REGISTER" -> 
        // 3 registrations per hour per IP (spam prevention)
        new RateLimit(3, Duration.ofHours(1));
      case "PASSWORD_RESET" -> 
        // 2 password reset requests per hour per IP
        new RateLimit(2, Duration.ofHours(1));
      case "EIDAS_LOGIN" -> 
        // 5 eIDAS login attempts per hour per IP
        new RateLimit(5, Duration.ofHours(1));
      default -> 
        // Default: 30 requests per minute for public endpoints
        new RateLimit(30, Duration.ofMinutes(1));
    };
  }

  /**
   * Data class for rate limit configuration.
   */
  public record RateLimit(int capacity, Duration refillDuration) {}
}
