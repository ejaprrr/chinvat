package eu.alboranplus.chinvat.health.infrastructure;

import eu.alboranplus.chinvat.health.application.SystemHealthPort;
import eu.alboranplus.chinvat.health.domain.SystemHealth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Infrastructure adapter: implementuje SystemHealthPort.
 *
 * @Component = Spring bean (alternativa k @Service pro ne-service tridy).
 * Spring vidi ze GetSystemHealthUseCase potrebuje SystemHealthPort,
 * najde tuto implementaci a automaticky ji injektuje — to je DI v praxi.
 *
 * Kdyz budes chtit unit testovat GetSystemHealthUseCase,
 * jednoduche predas mock implementaci SystemHealthPort.
 */
@Component
public class SystemHealthAdapter implements SystemHealthPort {

    // @Value cte hodnotu z application.properties / env var.
    // Defaultni hodnota za ":" pokud promenna neexistuje.
    @Value("${spring.application.name:chinvat}")
    private String appName;

    @Value("${APP_VERSION:0.0.1-SNAPSHOT}")
    private String version;

    @Override
    public SystemHealth check() {
        // Tady muzes pridat realne checks: DB ping, cache check, externi API ping atd.
        return new SystemHealth(
                SystemHealth.Status.UP,
                version,
                Instant.now()
        );
    }
}

