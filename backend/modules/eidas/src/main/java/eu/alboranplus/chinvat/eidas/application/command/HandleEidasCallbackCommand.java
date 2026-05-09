package eu.alboranplus.chinvat.eidas.application.command;

public record HandleEidasCallbackCommand(
    String providerCode,
    String state,
    String authorizationCode,
    String externalSubjectId,
    String levelOfAssurance) {}
