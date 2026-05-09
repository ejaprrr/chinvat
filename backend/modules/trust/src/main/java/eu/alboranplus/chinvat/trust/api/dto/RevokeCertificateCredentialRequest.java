package eu.alboranplus.chinvat.trust.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RevokeCertificateCredentialRequest(
    @NotBlank(message = "reason is required") String reason) {}
