package eu.alboranplus.chinvat.health.application;

import eu.alboranplus.chinvat.health.domain.SystemHealth;
import org.springframework.stereotype.Service;

@Service
public class GetSystemHealthUseCase {

  private final SystemHealthPort healthPort;

  public GetSystemHealthUseCase(SystemHealthPort healthPort) {
    this.healthPort = healthPort;
  }

  public SystemHealth execute() {
    return healthPort.check();
  }
}
