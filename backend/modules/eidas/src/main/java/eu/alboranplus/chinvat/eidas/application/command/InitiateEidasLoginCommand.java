package eu.alboranplus.chinvat.eidas.application.command;

public record InitiateEidasLoginCommand(String providerCode, String redirectUri) {}
