package eu.alboranplus.chinvat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security konfigurace.
 *
 * Aktualne: health endpoint je verejny, vsechno ostatni vyzaduje autentizaci.
 * TODO: az pridame JWT, tady ho zapojime.
 *
 * @Configuration = Spring bean definice (config trida)
 * @EnableWebSecurity = zapne Spring Security konfiguraci
 *
 * DI v praxi: Spring injektuje HttpSecurity do metody anotovane @Bean.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Vypnout CSRF pro REST API (stateless — token-based auth pozdeji)
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — zadne server-side session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Pravidla pristupnosti
            .authorizeHttpRequests(auth -> auth
                // Health endpoint — verejny (pro load balancer, Docker healthcheck, monitoring)
                .requestMatchers("/api/v1/health").permitAll()
                // H2 console — jen pro local profil (v prod nebezi)
                .requestMatchers("/h2-console/**").permitAll()
                // Vsechno ostatni vyzaduje autentizaci
                .anyRequest().authenticated()
            )

            // Povolit H2 konzoli v iframu (jen lokalne)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}

