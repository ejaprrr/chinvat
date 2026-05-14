package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Certificate validation request")
public record ValidateCertificateRequest(
    @Schema(description = "PEM-encoded X.509 certificate to validate")
        @NotBlank(message = "certificatePem is required") String certificatePem,
    @Schema(description = "When true, refreshes the trusted provider list before validation")
        boolean refreshTrustedProvidersBeforeValidation) {}
