package eu.alboranplus.chinvat.health.api;

import eu.alboranplus.chinvat.health.application.GetSystemHealthUseCase;
import eu.alboranplus.chinvat.health.domain.SystemHealth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health", description = "System health monitoring")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  private final GetSystemHealthUseCase getSystemHealthUseCase;

  public HealthController(GetSystemHealthUseCase getSystemHealthUseCase) {
    this.getSystemHealthUseCase = getSystemHealthUseCase;
  }

  @Operation(
      summary = "System health check",
      description = "Returns the current health status of the system. No authentication required.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "System is healthy",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description = "System is degraded or unavailable",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class)))
  })
  @SecurityRequirement(name = "")
  @GetMapping
  public ResponseEntity<HealthResponse> health() {
    SystemHealth health = getSystemHealthUseCase.execute();
    HealthResponse response = HealthResponse.from(health);

    return health.isHealthy()
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(503).body(response);
  }
}
