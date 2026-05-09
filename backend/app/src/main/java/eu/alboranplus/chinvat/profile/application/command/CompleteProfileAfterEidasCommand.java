package eu.alboranplus.chinvat.profile.application.command;

public record CompleteProfileAfterEidasCommand(
    String providerCode,
    String externalSubjectId,
    String username,
    String fullName,
    String phoneNumber,
    String email,
    String password,
    String addressLine,
    String postalCode,
    String city,
    String country,
    String defaultLanguage,
    String certificatePem,
    String assuranceLevel,
    String certificateProviderCode,
    String identityReference,
    String nationality) {}
