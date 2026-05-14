package eu.alboranplus.chinvat.eidas.infrastructure.broker.clave;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Token endpoint error response (RFC 6749 §5.2). */
record ClaveTokenErrorResponse(
    String error,
    @JsonProperty("error_description") String errorDescription) {}
