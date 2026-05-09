package eu.alboranplus.chinvat.profile.application.command;

public record AddProfileCertificateCommand(
    String certificatePem, String providerCode, String assuranceLevel) {}
