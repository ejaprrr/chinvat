package eu.alboranplus.chinvat.eidas.application.command;

import java.util.UUID;

public record CompleteEidasProfileCommand(
    String providerCode,
    String externalSubjectId,
    UUID userId,
    String identityReference,
    String nationality) {}
