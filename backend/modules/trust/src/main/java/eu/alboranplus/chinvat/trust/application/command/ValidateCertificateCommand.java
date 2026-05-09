package eu.alboranplus.chinvat.trust.application.command;

public record ValidateCertificateCommand(
    String certificatePem,
    boolean refreshTrustedProvidersBeforeValidation) {}
