package eu.alboranplus.chinvat.trust.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateCertificateRequest(
    @NotBlank(message = "certificatePem is required") String certificatePem,
    boolean refreshTrustedProvidersBeforeValidation) {}
