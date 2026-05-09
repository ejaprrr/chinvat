package eu.alboranplus.chinvat.trust.application.command;

public record BindCertificateCredentialCommand(
    Long userId,
    String providerCode,
    String registrationSource,
    String assuranceLevel,
    String certificatePem) {}
