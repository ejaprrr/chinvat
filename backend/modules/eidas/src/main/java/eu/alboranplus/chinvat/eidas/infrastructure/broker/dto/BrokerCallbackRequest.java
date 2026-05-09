package eu.alboranplus.chinvat.eidas.infrastructure.broker.dto;

public record BrokerCallbackRequest(
    String providerCode, String state, String authorizationCode) {}