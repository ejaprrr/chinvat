package eu.alboranplus.chinvat.eidas.application.dto;

public record EidasBrokerIdentityView(
    String externalSubjectId,
    String levelOfAssurance,
    String personIdentifier,
    String legalPersonIdentifier,
    String firstName,
    String familyName,
    String dateOfBirth) {}