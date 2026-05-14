package eu.alboranplus.chinvat.eidas.infrastructure.broker.clave;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasBrokerException;
import eu.alboranplus.chinvat.eidas.infrastructure.config.ClaveProperties;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * eIDAS broker adapter that integrates directly with Cl@ve 2.0 via OpenID Connect.
 *
 * <h2>Flow</h2>
 *
 * <ol>
 *   <li>{@link #initiateLogin} builds an OIDC Authorization Request URL (RFC 6749 + OIDC Core
 *       §3.1.2) pointing at the Cl@ve authorization endpoint and returns it to the frontend.
 *   <li>The user authenticates at Cl@ve (Spanish DNI/NIE, or EU eIDAS cross-border).
 *   <li>Cl@ve redirects the browser to the registered {@code redirect_uri} with {@code code} and
 *       {@code state}.
 *   <li>{@link #exchangeAuthorizationCode} calls the token endpoint (client_secret_basic), obtains
 *       the ID token, validates its signature (JWKS), issuer, audience, expiry, and nonce, then
 *       extracts the identity claims.
 * </ol>
 *
 * <h2>Nonce strategy</h2>
 *
 * The OIDC nonce is derived deterministically as {@code HMAC-SHA256(state, clientSecret)},
 * hex-encoded. This avoids extra server-side storage while remaining cryptographically bound to
 * both the per-request state and the server secret. An attacker who does not know {@code
 * clientSecret} cannot forge a valid nonce.
 *
 * <h2>Activation</h2>
 *
 * Active when {@code chinvat.clave.enabled=true} <em>and</em> the Spring profile is not {@code
 * dev} or {@code local}. This ensures the mock adapter always wins in local development.
 */
@Profile("!(dev | local)")
@ConditionalOnProperty(prefix = "chinvat.clave", name = "enabled", havingValue = "true")
@Repository
public class ClaveBrokerAdapter implements EidasBrokerPort {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private final ClaveProperties claveProperties;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final JwtDecoder jwtDecoder;

  public ClaveBrokerAdapter(
      ClaveProperties claveProperties,
      @Qualifier("claveHttpClient") HttpClient httpClient,
      ObjectMapper objectMapper,
      @Qualifier("claveJwtDecoder") JwtDecoder jwtDecoder) {
    this.claveProperties = claveProperties;
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.jwtDecoder = jwtDecoder;
  }

  // ---------------------------------------------------------------------------
  // EidasBrokerPort — provider registry
  // ---------------------------------------------------------------------------

  @Override
  public List<EidasProviderView> listProviders() {
    return claveProperties.getProviders().stream()
        .map(
            p ->
                new EidasProviderView(
                    p.getCode(), p.getDisplayName(), p.getCountryCode(), p.isEnabled()))
        .toList();
  }

  @Override
  public Optional<EidasProviderView> findEnabledProvider(String providerCode) {
    if (providerCode == null || providerCode.isBlank()) {
      return Optional.empty();
    }
    String normalized = providerCode.toUpperCase(Locale.ROOT);
    return listProviders().stream()
        .filter(EidasProviderView::enabled)
        .filter(p -> p.code().toUpperCase(Locale.ROOT).equals(normalized))
        .findFirst();
  }

  // ---------------------------------------------------------------------------
  // EidasBrokerPort — login initiation
  // ---------------------------------------------------------------------------

  /**
   * Builds the OIDC Authorization Request URL.
   *
   * <p>The {@code nonce} is derived from {@code state} via HMAC-SHA256 so it never requires
   * independent storage. The frontend must redirect the user to the returned {@code
   * authorizationUrl}.
   */
  @Override
  public EidasLoginView initiateLogin(
      String providerCode, String redirectUri, String state, Instant expiresAt) {
    String nonce = computeNonce(state);
    String authorizationUrl = buildAuthorizationUrl(state, nonce);
    return new EidasLoginView(providerCode, state, authorizationUrl, expiresAt);
  }

  // ---------------------------------------------------------------------------
  // EidasBrokerPort — callback / code exchange
  // ---------------------------------------------------------------------------

  /**
   * Exchanges the OIDC authorization code for an ID token and extracts the eIDAS identity.
   *
   * <p>Validation performed:
   *
   * <ul>
   *   <li>ID token signature (JWKS key rotation handled by {@link JwtDecoder}).
   *   <li>Issuer ({@code iss}) — must equal {@code chinvat.clave.issuer-uri}.
   *   <li>Audience ({@code aud}) — must contain {@code chinvat.clave.client-id}.
   *   <li>Expiry ({@code exp}).
   *   <li>Nonce — constant-time comparison against HMAC-SHA256(state, clientSecret).
   * </ul>
   */
  @Override
  public EidasBrokerIdentityView exchangeAuthorizationCode(
      String providerCode, String state, String authorizationCode) {
    ClaveTokenResponse tokens = fetchToken(authorizationCode);
    Jwt jwt = decodeIdToken(tokens.idToken());
    validateNonce(jwt, state);
    return extractIdentity(jwt);
  }

  // ---------------------------------------------------------------------------
  // Private — OIDC authorization URL
  // ---------------------------------------------------------------------------

  private String buildAuthorizationUrl(String state, String nonce) {
    return UriComponentsBuilder.fromUriString(claveProperties.getAuthorizationEndpoint())
        .queryParam("response_type", "code")
        .queryParam("client_id", claveProperties.getClientId())
        .queryParam("redirect_uri", claveProperties.getRedirectUri())
        .queryParam("scope", claveProperties.getScope())
        .queryParam("state", state)
        .queryParam("nonce", nonce)
        .queryParam("acr_values", claveProperties.getAcrValues())
        .build()
        .toUriString();
  }

  // ---------------------------------------------------------------------------
  // Private — token endpoint
  // ---------------------------------------------------------------------------

  private ClaveTokenResponse fetchToken(String authorizationCode) {
    String requestBody =
        "grant_type=authorization_code"
            + "&code="
            + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8)
            + "&redirect_uri="
            + URLEncoder.encode(claveProperties.getRedirectUri(), StandardCharsets.UTF_8);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(claveProperties.getTokenEndpoint()))
            .timeout(Duration.ofMillis(claveProperties.getReadTimeoutMillis()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .header("Authorization", buildBasicAuthHeader())
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new EidasBrokerException("Cl@ve token endpoint I/O failure", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new EidasBrokerException("Cl@ve token endpoint request interrupted", e);
    }

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new EidasBrokerException(
          "Cl@ve token endpoint returned HTTP "
              + response.statusCode()
              + describeTokenError(response.body()));
    }

    try {
      return objectMapper.readValue(response.body(), ClaveTokenResponse.class);
    } catch (JsonProcessingException e) {
      throw new EidasBrokerException("Failed to deserialise Cl@ve token response", e);
    }
  }

  /** Builds an RFC 7617 {@code Basic} authorization header value. */
  private String buildBasicAuthHeader() {
    String credentials =
        URLEncoder.encode(claveProperties.getClientId(), StandardCharsets.UTF_8)
            + ":"
            + URLEncoder.encode(claveProperties.getClientSecret(), StandardCharsets.UTF_8);
    return "Basic "
        + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  private String describeTokenError(String body) {
    try {
      ClaveTokenErrorResponse error = objectMapper.readValue(body, ClaveTokenErrorResponse.class);
      String detail = ": " + error.error();
      if (error.errorDescription() != null && !error.errorDescription().isBlank()) {
        detail += " — " + error.errorDescription();
      }
      return detail;
    } catch (JsonProcessingException ignored) {
      int end = Math.min(200, body.length());
      return ": " + body.substring(0, end);
    }
  }

  // ---------------------------------------------------------------------------
  // Private — ID token validation
  // ---------------------------------------------------------------------------

  private Jwt decodeIdToken(String idToken) {
    try {
      return jwtDecoder.decode(idToken);
    } catch (JwtException e) {
      throw new EidasBrokerException("Cl@ve ID token validation failed: " + e.getMessage(), e);
    }
  }

  /**
   * Validates the {@code nonce} claim using constant-time comparison.
   *
   * <p>The expected nonce is recomputed on the fly as HMAC-SHA256(state, clientSecret), matching
   * the value produced in {@link #initiateLogin}. Constant-time equality prevents timing-based
   * oracle attacks.
   */
  private void validateNonce(Jwt jwt, String state) {
    String expected = computeNonce(state);
    String actual = jwt.getClaimAsString("nonce");
    if (actual == null) {
      throw new EidasBrokerException(
          "Cl@ve ID token is missing the required 'nonce' claim — potential replay attack");
    }
    boolean nonceValid =
        MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            actual.getBytes(StandardCharsets.UTF_8));
    if (!nonceValid) {
      throw new EidasBrokerException(
          "Cl@ve ID token nonce mismatch — potential replay attack detected");
    }
  }

  /**
   * Derives the OIDC nonce deterministically as {@code hex(HMAC-SHA256(state, clientSecret))}.
   *
   * <p>Using the client secret as the HMAC key ensures that only this server can produce or verify
   * the nonce, while the per-request {@code state} ensures uniqueness.
   */
  private String computeNonce(String state) {
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(
          new SecretKeySpec(
              claveProperties.getClientSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
      byte[] digest = mac.doFinal(state.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new EidasBrokerException("Failed to compute OIDC nonce", e);
    }
  }

  // ---------------------------------------------------------------------------
  // Private — identity extraction
  // ---------------------------------------------------------------------------

  /**
   * Extracts eIDAS identity attributes from the validated ID token.
   *
   * <p>For EU citizens authenticating via the cross-border eIDAS network through Cl@ve, the eIDAS
   * PersonIdentifier claim takes precedence. For Spanish nationals the NIF/NIE ({@code nin}) is
   * used as the person identifier.
   */
  private EidasBrokerIdentityView extractIdentity(Jwt jwt) {
    ClaveProperties.ClaimNames names = claveProperties.getClaimNames();

    String personIdentifier = jwt.getClaimAsString(names.getEidasPersonIdentifier());
    if (personIdentifier == null || personIdentifier.isBlank()) {
      personIdentifier = jwt.getClaimAsString(names.getPersonIdentifier());
    }

    return new EidasBrokerIdentityView(
        jwt.getSubject(),
        jwt.getClaimAsString(names.getLevelOfAssurance()),
        personIdentifier,
        null, // legalPersonIdentifier — not applicable for natural persons
        jwt.getClaimAsString(names.getFirstName()),
        jwt.getClaimAsString(names.getFamilyName()),
        jwt.getClaimAsString(names.getDateOfBirth()));
  }
}
