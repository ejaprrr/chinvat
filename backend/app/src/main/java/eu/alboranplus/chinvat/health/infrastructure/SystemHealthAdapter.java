package eu.alboranplus.chinvat.health.infrastructure;

import eu.alboranplus.chinvat.health.application.SystemHealthPort;
import eu.alboranplus.chinvat.health.domain.SystemHealth;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemHealthAdapter implements SystemHealthPort {

  @Value("${APP_VERSION:0.0.1-SNAPSHOT}")
  private String version;

  @Override
  public SystemHealth check() {
    return new SystemHealth(SystemHealth.Status.UP, version, Instant.now());
  }
}
