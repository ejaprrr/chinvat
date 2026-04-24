package eu.alboranplus.chinvat.health.domain;

import java.time.Instant;

/**
 * Domain model representing the overall health of the system.
 * Pure Java — no Spring, no JPA, no HTTP dependencies.
 */
public record SystemHealth(
        Status status,
        String version,
        Instant checkedAt
) {

    public enum Status {
        UP, DEGRADED, DOWN
    }

    public boolean isHealthy() {
        return status == Status.UP;
    }
}

