package eu.alboranplus.chinvat.config;

import eu.alboranplus.chinvat.security.BearerTokenAuthFilter;
import eu.alboranplus.chinvat.security.RateLimitingFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final BearerTokenAuthFilter bearerTokenAuthFilter;
  private final RateLimitingFilter rateLimitingFilter;

  @Value("${app.cors.allowed-origins}")
  private List<String> allowedOrigins;

  public SecurityConfig(BearerTokenAuthFilter bearerTokenAuthFilter, RateLimitingFilter rateLimitingFilter) {
    this.bearerTokenAuthFilter = bearerTokenAuthFilter;
    this.rateLimitingFilter = rateLimitingFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/health")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/fnmt/login")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/refresh")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/logout")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/register")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/password-reset/request")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/password-reset/confirm")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/eidas/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/profile/eidas/complete")
                    .permitAll()
                    .requestMatchers("/api/v1/profile/eidas/complete")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
