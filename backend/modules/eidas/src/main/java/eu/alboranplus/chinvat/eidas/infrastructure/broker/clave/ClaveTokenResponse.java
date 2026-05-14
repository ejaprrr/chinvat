package eu.alboranplus.chinvat.eidas.infrastructure.broker.clave;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Token endpoint success response (RFC 6749 §5.1 + OIDC Core §3.1.3.3). */
record ClaveTokenResponse(
    @JsonProperty("id_token") String idToken,
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") Long expiresIn) {}
