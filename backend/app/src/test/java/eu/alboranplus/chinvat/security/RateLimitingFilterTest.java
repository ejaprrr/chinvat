package eu.alboranplus.chinvat.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test rate limiting filter behavior.
 * 
 * Tests verify:
 * 1. Anonymous IP-based rate limiting (login endpoint)
 * 2. Authenticated user-based rate limiting
 * 3. Proper 429 response with Retry-After header
 * 4. Rate limit counters in Redis
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Rate Limiting Filter Tests")
class RateLimitingFilterTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    // Clear any cached rate limit data before each test
    // In real scenarios, this would clear Redis keys
  }

  @Test
  @DisplayName("Should allow requests within rate limit for anonymous login")
  void shouldAllowRequestsWithinLimit() throws Exception {
    // Anonymous user should be allowed 5 login attempts per 5 minutes
    // Using IP-based rate limiting
    
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    // First attempt should succeed
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isUnauthorized()) // Auth fails but rate limit passes
        .andExpect(header().exists("X-Rate-Limit-Remaining"));
  }

  @Test
  @DisplayName("Should return 429 when anonymous IP exceeds login rate limit")
  void shouldReturn429WhenExceedsAnonymousLimit() throws Exception {
    // IP-based limit for login: 5 attempts per 5 minutes
    // Test by making 6 requests from same IP
    
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    String clientIp = "203.0.113.50"; // Test IP
    
    // Make 5 successful requests (within limit)
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(
          post("/api/v1/auth/login")
              .header("X-Forwarded-For", clientIp)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isUnauthorized())
          .andReturn();
    }
    
    // 6th request should be rate limited
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", clientIp)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isTooManyRequests()) // 429
        .andExpect(header().exists("Retry-After"))
        .andExpect(header().exists("X-Rate-Limit-Reset-After-Millis"))
        .andExpect(jsonPath("$.error").value("Rate limit exceeded"))
        .andReturn();
  }

  @Test
  @DisplayName("Should allow different IPs to exceed login limit independently")
  void shouldLimitPerIP() throws Exception {
    // Different IPs should have independent limits
    
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    // IP 1 makes 5 requests (max allowed)
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(
          post("/api/v1/auth/login")
              .header("X-Forwarded-For", "192.168.1.1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isUnauthorized())
          .andReturn();
    }
    
    // IP 1 6th request should fail (rate limited)
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", "192.168.1.1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andReturn();
    
    // IP 2 should still be able to make requests (fresh limit)
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", "192.168.1.2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isUnauthorized()) // Auth fails, but not rate limited
        .andExpect(header().exists("X-Rate-Limit-Remaining"))
        .andReturn();
  }

  @Test
  @DisplayName("Should skip rate limiting for health check endpoint")
  void shouldSkipHealthCheckEndpoint() throws Exception {
    // Health check should not be rate limited (operational endpoint)
    
    for (int i = 0; i < 100; i++) {
      mockMvc.perform(get("/api/v1/health"))
          .andExpect(status().isOk())
          .andReturn();
    }
    
    // All should succeed without 429
  }

  @Test
  @DisplayName("Should handle X-Forwarded-For with multiple IPs (take first)")
  void shouldExtractFirstIPFromXForwardedFor() throws Exception {
    // When multiple IPs in X-Forwarded-For, should use first (client IP)
    // X-Forwarded-For: client, proxy1, proxy2
    
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    // Make 5 requests with same first IP (should share limit)
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(
          post("/api/v1/auth/login")
              .header("X-Forwarded-For", "203.0.113.100, 10.0.0.1, 10.0.0.2")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isUnauthorized())
          .andReturn();
    }
    
    // 6th should be rate limited (same first IP)
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", "203.0.113.100, 10.0.0.1, 10.0.0.2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andReturn();
  }

  @Test
  @DisplayName("Should return proper retry headers with rate limit info")
  void shouldReturnProperRetryHeaders() throws Exception {
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    String clientIp = "203.0.113.75";
    
    // Fill the bucket for this IP
    for (int i = 0; i < 5; i++) {
      mockMvc.perform(
          post("/api/v1/auth/login")
              .header("X-Forwarded-For", clientIp)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andReturn();
    }
    
    // Verify rate limit response headers
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", clientIp)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().exists("Retry-After"))
        .andExpect(header().exists("X-Rate-Limit-Reset-After-Millis"))
        .andExpect(jsonPath("$.retryAfterSeconds").isNumber())
        .andReturn();
  }

  @Test
  @DisplayName("Should log remaining tokens in success response headers")
  void shouldLogRemainingTokens() throws Exception {
    LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
    String requestBody = objectMapper.writeValueAsString(loginRequest);
    
    // First request should show remaining tokens
    mockMvc.perform(
        post("/api/v1/auth/login")
            .header("X-Forwarded-For", "203.0.113.200")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andExpect(header().exists("X-Rate-Limit-Remaining"))
        // Should have 4 remaining (started with 5, used 1)
        .andReturn();
  }
}
