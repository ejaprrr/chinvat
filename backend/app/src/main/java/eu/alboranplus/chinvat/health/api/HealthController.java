package eu.alboranplus.chinvat.health.api;

import eu.alboranplus.chinvat.health.application.GetSystemHealthUseCase;
import eu.alboranplus.chinvat.health.domain.SystemHealth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  private final GetSystemHealthUseCase getSystemHealthUseCase;

  public HealthController(GetSystemHealthUseCase getSystemHealthUseCase) {
    this.getSystemHealthUseCase = getSystemHealthUseCase;
  }

  @GetMapping
  public ResponseEntity<HealthResponse> health() {
    SystemHealth health = getSystemHealthUseCase.execute();
    HealthResponse response = HealthResponse.from(health);

    return health.isHealthy()
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(503).body(response);
  }
}
