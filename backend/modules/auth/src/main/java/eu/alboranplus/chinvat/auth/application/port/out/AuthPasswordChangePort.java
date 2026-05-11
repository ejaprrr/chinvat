package eu.alboranplus.chinvat.auth.application.port.out;

import java.util.UUID;

public interface AuthPasswordChangePort {
  void changePassword(UUID userId, String rawPassword);
}

