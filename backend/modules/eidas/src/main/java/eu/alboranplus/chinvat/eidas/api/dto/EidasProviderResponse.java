package eu.alboranplus.chinvat.eidas.api.dto;

public record EidasProviderResponse(
    String code,
    String displayName,
    String countryCode,
    boolean enabled) {}
