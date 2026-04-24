package eu.alboranplus.chinvat.users.infrastructure.clock;

import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class UsersClockAdapter implements UsersClockPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
