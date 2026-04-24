package eu.alboranplus.chinvat.health.domain;

import java.time.Instant;

public record SystemHealth(Status status, String version, Instant checkedAt) {

  public enum Status {
    UP,
    DEGRADED,
    DOWN
  }

  public boolean isHealthy() {
    return status == Status.UP;
  }
}
