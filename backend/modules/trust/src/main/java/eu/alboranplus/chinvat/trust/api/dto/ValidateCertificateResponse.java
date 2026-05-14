package eu.alboranplus.chinvat.trust.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Certificate validation result")
public record ValidateCertificateResponse(
    @Schema(description = "SHA-256 thumbprint of the certificate") String thumbprintSha256,
    @Schema(description = "Subject distinguished name") String subjectDn,
    @Schema(description = "Issuer distinguished name") String issuerDn,
    @Schema(description = "Certificate serial number") String serialNumber,
    @Schema(description = "Certificate valid-from timestamp (UTC)") Instant notBefore,
    @Schema(description = "Certificate valid-until timestamp (UTC)") Instant notAfter,
    @Schema(description = "True if the certificate is currently within its validity period") boolean validNow,
    @Schema(description = "True if the issuer is present in the trusted provider list") boolean trustedIssuer,
    @Schema(description = "Source used to determine trust", example = "TSL") String trustSource,
    @Schema(description = "Key usage flags declared in the certificate",
        example = "[\"digitalSignature\",\"nonRepudiation\"]") List<String> keyUsageFlags,
    @Schema(description = "Timestamp when the validation was performed (UTC)") Instant validatedAt) {}
