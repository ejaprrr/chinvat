package eu.alboranplus.chinvat.eidas.infrastructure.broker.dto;

public record BrokerProviderResponse(
    String code, String displayName, String countryCode, boolean enabled) {}