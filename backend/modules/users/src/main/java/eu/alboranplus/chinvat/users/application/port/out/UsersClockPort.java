package eu.alboranplus.chinvat.users.application.port.out;

import java.time.Instant;

public interface UsersClockPort {
  Instant now();
}
