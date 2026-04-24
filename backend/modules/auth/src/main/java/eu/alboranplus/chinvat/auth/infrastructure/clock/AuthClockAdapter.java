package eu.alboranplus.chinvat.auth.infrastructure.clock;

import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class AuthClockAdapter implements AuthClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
