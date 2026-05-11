package eu.alboranplus.chinvat.trust.application.command;

import java.util.UUID;

public record BindCertificateCredentialCommand(
    UUID userId,
    String providerCode,
    String registrationSource,
    String assuranceLevel,
    String certificatePem) {}
