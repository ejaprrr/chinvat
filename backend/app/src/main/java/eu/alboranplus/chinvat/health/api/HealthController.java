package eu.alboranplus.chinvat.health.api;

import eu.alboranplus.chinvat.health.application.GetSystemHealthUseCase;
import eu.alboranplus.chinvat.health.domain.SystemHealth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller — jen vstup/vystup HTTP.
 * Zadna business logika tady. Vsechna logika je v use-case.
 *
 * @RestController = @Controller + @ResponseBody (Spring automaticky serializuje na JSON)
 *
 * DI v praxi:
 * - Spring vytvori GetSystemHealthUseCase bean
 * - Spring vytvori HealthController bean
 * - Spring vidi ze HealthController potrebuje GetSystemHealthUseCase
 * - Spring ho automaticky injektuje pres konstruktor
 * - Ty jsi nikde nenapsal "new GetSystemHealthUseCase(...)" — to je DI
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final GetSystemHealthUseCase getSystemHealthUseCase;

    public HealthController(GetSystemHealthUseCase getSystemHealthUseCase) {
        this.getSystemHealthUseCase = getSystemHealthUseCase;
    }

    /**
     * GET /api/v1/health
     * Returns 200 OK when healthy, 503 SERVICE_UNAVAILABLE when not.
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        SystemHealth health = getSystemHealthUseCase.execute();
        HealthResponse response = HealthResponse.from(health);

        return health.isHealthy()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(503).body(response);
    }
}

