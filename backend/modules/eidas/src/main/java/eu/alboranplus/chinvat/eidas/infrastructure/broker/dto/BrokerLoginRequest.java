package eu.alboranplus.chinvat.eidas.infrastructure.broker.dto;

public record BrokerLoginRequest(
    String providerCode, String redirectUri, String state, String expiresAt) {}