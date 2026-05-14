package eu.alboranplus.chinvat.eidas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of processing the eIDAS callback")
public record EidasCallbackResponse(
    @Schema(description = "Provider code", example = "EIDAS_EU") String providerCode,
    @Schema(description = "External subject identifier from the eIDAS node", example = "ES/ES/12345678A") String externalSubjectId,
    @Schema(description = "Level of assurance", example = "http://eidas.europa.eu/LoA/high") String levelOfAssurance,
    @Schema(description = "Current identity linking status", example = "LINKED") String currentStatus,
    @Schema(description = "Linked user UUID (null if the identity is not yet linked to any account)",
        type = "string", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000",
        nullable = true) UUID linkedUserId,
    @Schema(description = "True when the user must complete profile registration before logging in") boolean profileCompletionRequired,
    @Schema(description = "Timestamp when the callback was processed (UTC)") Instant processedAt) {}
