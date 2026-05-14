package eu.alboranplus.chinvat.profile.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Result of completing a user profile after eIDAS authentication")
public record CompleteEidasProfileResponse(
    @Schema(description = "Newly created user UUID", type = "string", format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000") UUID userId,
    @Schema(description = "eIDAS provider code", example = "EIDAS_EU") String providerCode,
    @Schema(description = "External subject identifier assigned by the eIDAS node",
        example = "ES/ES/12345678A") String externalSubjectId,
    @Schema(description = "Current identity linking status", example = "LINKED") String currentStatus,
    @Schema(description = "Timestamp when the external identity was linked (UTC)") Instant linkedAt,
    @Schema(description = "Timestamp when profile completion was processed (UTC)") Instant completedAt) {}
