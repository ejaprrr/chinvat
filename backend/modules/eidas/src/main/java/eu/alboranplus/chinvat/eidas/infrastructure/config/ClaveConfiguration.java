package eu.alboranplus.chinvat.eidas.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Spring configuration for the Cl@ve 2.0 OIDC integration.
 *
 * <p>Only activated when {@code chinvat.clave.enabled=true}. Registers:
 *
 * <ul>
 *   <li>{@code claveHttpClient} — a dedicated {@link HttpClient} tuned for Cl@ve endpoint calls.
 *       Redirect following is disabled to prevent SSRF via open-redirect responses.
 *   <li>{@code claveJwtDecoder} — a {@link NimbusJwtDecoder} backed by the configured JWKS URI,
 *       with issuer and audience validators wired in.
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(ClaveProperties.class)
@ConditionalOnProperty(prefix = "chinvat.clave", name = "enabled", havingValue = "true")
public class ClaveConfiguration {

  /**
   * Dedicated {@link HttpClient} for Cl@ve OIDC token endpoint calls.
   *
   * <p>{@code followRedirects} is set to {@link HttpClient.Redirect#NEVER} to prevent an
   * open-redirect at the token endpoint from being silently followed and leaking the authorization
   * code.
   */
  @Bean("claveHttpClient")
  public HttpClient claveHttpClient(ClaveProperties claveProperties) {
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(claveProperties.getConnectTimeoutMillis()))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
  }

  /**
   * {@link JwtDecoder} for Cl@ve ID token validation.
   *
   * <p>Validates:
   *
   * <ul>
   *   <li>Signature — RS256/ES256 verified against the live JWKS (key rotation handled
   *       automatically by Nimbus).
   *   <li>Issuer ({@code iss}) — must equal {@code chinvat.clave.issuer-uri}.
   *   <li>Audience ({@code aud}) — must contain {@code chinvat.clave.client-id}.
   *   <li>Expiry ({@code exp}) and Not-Before ({@code nbf}).
   * </ul>
   *
   * <p>Nonce validation is intentionally excluded here and handled in {@code ClaveBrokerAdapter}
   * because the expected nonce is state-dependent (derived per-request).
   *
   * <p>The qualifier {@code "claveJwtDecoder"} avoids collision with any other {@link JwtDecoder}
   * bean that may be registered by application modules.
   */
  @Bean("claveJwtDecoder")
  @Qualifier("claveJwtDecoder")
  public JwtDecoder claveJwtDecoder(ClaveProperties claveProperties) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri(claveProperties.getJwksUri()).build();

    OAuth2TokenValidator<Jwt> issuerValidator =
        JwtValidators.createDefaultWithIssuer(claveProperties.getIssuerUri());

    OAuth2TokenValidator<Jwt> audienceValidator =
        new JwtClaimValidator<List<String>>(
            "aud",
            aud -> aud != null && aud.contains(claveProperties.getClientId()));

    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));

    return decoder;
  }
}
