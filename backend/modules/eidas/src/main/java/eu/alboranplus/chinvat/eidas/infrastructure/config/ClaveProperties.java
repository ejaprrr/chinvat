package eu.alboranplus.chinvat.eidas.infrastructure.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chinvat.clave")
public class ClaveProperties {

  /** Enable the Cl@ve 2.0 OIDC adapter (replaces generic HttpEidasBrokerAdapter). */
  private boolean enabled = false;

  /**
   * OIDC Issuer URI — used to validate the {@code iss} claim in ID tokens.
   *
   * <p>Obtain from {@code /.well-known/openid-configuration}.
   */
  private String issuerUri = "";

  /**
   * OIDC Authorization Endpoint — where users are redirected to authenticate.
   *
   * <p>Obtain from {@code /.well-known/openid-configuration#authorization_endpoint}.
   */
  private String authorizationEndpoint = "";

  /**
   * OIDC Token Endpoint — where the authorization code is exchanged for tokens.
   *
   * <p>Obtain from {@code /.well-known/openid-configuration#token_endpoint}.
   */
  private String tokenEndpoint = "";

  /**
   * JWKS URI — used to fetch public keys for ID token signature verification.
   *
   * <p>Obtain from {@code /.well-known/openid-configuration#jwks_uri}.
   */
  private String jwksUri = "";

  /** OAuth2 client_id assigned by FNMT/SEAD during SP registration. */
  private String clientId = "";

  /**
   * OAuth2 client_secret assigned by FNMT/SEAD during SP registration.
   *
   * <p>Must be supplied via environment variable; never committed to source control.
   */
  private String clientSecret = "";

  /**
   * Registered redirect URI. Must exactly match the value registered with Cl@ve.
   *
   * <p>Example: {@code https://your-app.example.com/auth/eidas/callback}
   */
  private String redirectUri = "";

  /** OIDC scopes to request. Default covers identity claims available in Cl@ve 2.0. */
  private String scope = "openid profile";

  /**
   * Required eIDAS Level of Assurance.
   *
   * <p>Space-separated list of acceptable ACR values in descending preference order.
   */
  private String acrValues = "http://eidas.europa.eu/LoA/high";

  /** TCP connect timeout for Cl@ve OIDC endpoint calls (milliseconds). */
  private long connectTimeoutMillis = 10_000L;

  /** Read timeout for Cl@ve token endpoint calls (milliseconds). */
  private long readTimeoutMillis = 30_000L;

  /** Identity claim name mapping — adjust if Cl@ve issues non-standard claim names. */
  private ClaimNames claimNames = new ClaimNames();

  /** List of eIDAS providers exposed by this Cl@ve integration. */
  private List<Provider> providers = new ArrayList<>(defaultProviders());

  // ---------------------------------------------------------------------------
  // Defaults
  // ---------------------------------------------------------------------------

  private static List<Provider> defaultProviders() {
    Provider es = new Provider();
    es.setCode("CLAVE_ES");
    es.setDisplayName("Cl@ve (España)");
    es.setCountryCode("ES");
    es.setEnabled(true);
    return List.of(es);
  }

  // ---------------------------------------------------------------------------
  // Getters / Setters
  // ---------------------------------------------------------------------------

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getIssuerUri() {
    return issuerUri;
  }

  public void setIssuerUri(String issuerUri) {
    this.issuerUri = issuerUri;
  }

  public String getAuthorizationEndpoint() {
    return authorizationEndpoint;
  }

  public void setAuthorizationEndpoint(String authorizationEndpoint) {
    this.authorizationEndpoint = authorizationEndpoint;
  }

  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  public String getJwksUri() {
    return jwksUri;
  }

  public void setJwksUri(String jwksUri) {
    this.jwksUri = jwksUri;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getAcrValues() {
    return acrValues;
  }

  public void setAcrValues(String acrValues) {
    this.acrValues = acrValues;
  }

  public long getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public void setConnectTimeoutMillis(long connectTimeoutMillis) {
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  public long getReadTimeoutMillis() {
    return readTimeoutMillis;
  }

  public void setReadTimeoutMillis(long readTimeoutMillis) {
    this.readTimeoutMillis = readTimeoutMillis;
  }

  public ClaimNames getClaimNames() {
    return claimNames;
  }

  public void setClaimNames(ClaimNames claimNames) {
    this.claimNames = claimNames;
  }

  public List<Provider> getProviders() {
    return providers;
  }

  public void setProviders(List<Provider> providers) {
    this.providers = providers;
  }

  // ---------------------------------------------------------------------------
  // Inner classes
  // ---------------------------------------------------------------------------

  /**
   * Configurable mapping from standard claim names to the names actually issued by Cl@ve.
   *
   * <p>Defaults are based on Cl@ve 2.0 OIDC specification and eIDAS attribute profile.
   */
  public static class ClaimNames {

    /** NIF/NIE for Spanish nationals. Falls back when eIDAS PersonIdentifier is absent. */
    private String personIdentifier = "nin";

    /**
     * eIDAS cross-border PersonIdentifier.
     *
     * <p>Present for EU citizens authenticating via the eIDAS network through Cl@ve.
     */
    private String eidasPersonIdentifier =
        "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier";

    private String firstName = "given_name";
    private String familyName = "family_name";
    private String dateOfBirth = "birthdate";

    /** ACR claim carrying the achieved Level of Assurance. */
    private String levelOfAssurance = "acr";

    public String getPersonIdentifier() {
      return personIdentifier;
    }

    public void setPersonIdentifier(String personIdentifier) {
      this.personIdentifier = personIdentifier;
    }

    public String getEidasPersonIdentifier() {
      return eidasPersonIdentifier;
    }

    public void setEidasPersonIdentifier(String eidasPersonIdentifier) {
      this.eidasPersonIdentifier = eidasPersonIdentifier;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getFamilyName() {
      return familyName;
    }

    public void setFamilyName(String familyName) {
      this.familyName = familyName;
    }

    public String getDateOfBirth() {
      return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
    }

    public String getLevelOfAssurance() {
      return levelOfAssurance;
    }

    public void setLevelOfAssurance(String levelOfAssurance) {
      this.levelOfAssurance = levelOfAssurance;
    }
  }

  /** An eIDAS provider accessible via this Cl@ve integration. */
  public static class Provider {

    private String code;
    private String displayName;
    private String countryCode;
    private boolean enabled;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    public String getCountryCode() {
      return countryCode;
    }

    public void setCountryCode(String countryCode) {
      this.countryCode = countryCode;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
