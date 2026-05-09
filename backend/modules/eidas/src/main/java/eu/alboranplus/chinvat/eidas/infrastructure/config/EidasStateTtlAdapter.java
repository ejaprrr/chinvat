package eu.alboranplus.chinvat.eidas.infrastructure.config;

import eu.alboranplus.chinvat.eidas.application.port.out.EidasStateTtlPort;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class EidasStateTtlAdapter implements EidasStateTtlPort {

  private final EidasProperties eidasProperties;

  public EidasStateTtlAdapter(EidasProperties eidasProperties) {
    this.eidasProperties = eidasProperties;
  }

  @Override
  public Duration stateTtl() {
    return eidasProperties.getStateTtl();
  }
}
