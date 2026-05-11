package eu.alboranplus.chinvat.trust.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BindCertificateCredentialRequest(
    @NotNull(message = "userId is required") UUID userId,
    @NotBlank(message = "providerCode is required") String providerCode,
    @NotBlank(message = "registrationSource is required") String registrationSource,
    String assuranceLevel,
    @NotBlank(message = "certificatePem is required") String certificatePem) {}
