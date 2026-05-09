package eu.alboranplus.chinvat.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProfileCertificateRequest(
    @NotBlank String certificatePem,
    @Size(max = 80) String providerCode,
    @Size(max = 80) String assuranceLevel) {}
