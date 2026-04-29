package eu.alboranplus.chinvat.config;

import eu.alboranplus.chinvat.security.BearerTokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final BearerTokenAuthFilter bearerTokenAuthFilter;

  public SecurityConfig(BearerTokenAuthFilter bearerTokenAuthFilter) {
    this.bearerTokenAuthFilter = bearerTokenAuthFilter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/health")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/login")
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
}
