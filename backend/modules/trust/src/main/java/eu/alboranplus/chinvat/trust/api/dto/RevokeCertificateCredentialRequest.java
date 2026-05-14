package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credential revocation payload")
public record RevokeCertificateCredentialRequest(
    @Schema(description = "Reason for revocation", example = "USER_REQUEST")
        @NotBlank(message = "reason is required") String reason) {}
