package eu.alboranplus.chinvat.eidas.application.command;

public record CompleteEidasProfileCommand(
    String providerCode,
    String externalSubjectId,
    Long userId,
    String identityReference,
    String nationality) {}
