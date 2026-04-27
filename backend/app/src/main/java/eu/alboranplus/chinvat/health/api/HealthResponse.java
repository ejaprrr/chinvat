package eu.alboranplus.chinvat.health.api;

import eu.alboranplus.chinvat.health.domain.SystemHealth;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "System health status")
public record HealthResponse(
    @Schema(description = "Health status", example = "UP", allowableValues = {"UP", "DOWN"})
        String status,
    @Schema(description = "Application version", example = "0.0.1-SNAPSHOT") String version,
    @Schema(description = "Timestamp when health was last checked") Instant checkedAt,
    @Schema(description = "Whether the system is considered healthy") boolean healthy) {
  public static HealthResponse from(SystemHealth health) {
    return new HealthResponse(
        health.status().name(), health.version(), health.checkedAt(), health.isHealthy());
  }
}
