package eu.alboranplus.chinvat.eidas.api.dto;

import java.time.Instant;

public record EidasApiErrorResponse(String message, Instant timestamp) {}
