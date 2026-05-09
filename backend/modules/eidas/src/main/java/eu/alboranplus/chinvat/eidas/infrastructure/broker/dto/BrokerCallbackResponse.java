package eu.alboranplus.chinvat.eidas.infrastructure.broker.dto;

public record BrokerCallbackResponse(
    String externalSubjectId,
    String levelOfAssurance,
    String personIdentifier,
    String legalPersonIdentifier,
    String firstName,
    String familyName,
    String dateOfBirth) {}