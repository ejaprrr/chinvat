package eu.alboranplus.chinvat.health.api;

import eu.alboranplus.chinvat.health.domain.SystemHealth;

import java.time.Instant;

/**
 * HTTP Response DTO — oddelene od domain modelu.
 * NIKDY nevracej domain object primo z REST controlleru.
 * Duvod: domain se meni nezavisle na HTTP kontraktu.
 */
public record HealthResponse(
        String status,
        String version,
        Instant checkedAt,
        boolean healthy
) {
    public static HealthResponse from(SystemHealth health) {
        return new HealthResponse(
                health.status().name(),
                health.version(),
                health.checkedAt(),
                health.isHealthy()
        );
    }
}

