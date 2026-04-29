package eu.alboranplus.chinvat.auth.api.dto;

import eu.alboranplus.chinvat.auth.domain.model.AuthSessionTokenKind;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Session information (safe projection)")
public record AuthSessionResponse(
    @Schema(description = "Session id", example = "d290f1ee-6c54-4b01-90e6-d701748f0851") UUID sessionId,
    @Schema(description = "Token kind (ACCESS/REFRESH)") AuthSessionTokenKind tokenKind,
    @Schema(description = "Issued at (UTC)") Instant issuedAt,
    @Schema(description = "Expires at (UTC)") Instant expiresAt,
    @Schema(description = "Client IP") String clientIp,
    @Schema(description = "User agent") String userAgent) {}

