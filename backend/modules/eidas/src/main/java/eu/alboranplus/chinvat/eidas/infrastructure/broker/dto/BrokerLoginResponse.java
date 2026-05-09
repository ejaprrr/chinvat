package eu.alboranplus.chinvat.eidas.infrastructure.broker.dto;

public record BrokerLoginResponse(String authorizationUrl, String expiresAt) {}