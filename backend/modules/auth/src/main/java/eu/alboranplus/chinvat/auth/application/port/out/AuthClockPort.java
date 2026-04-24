package eu.alboranplus.chinvat.auth.application.port.out;

import java.time.Instant;

public interface AuthClockPort {
  Instant now();
}
