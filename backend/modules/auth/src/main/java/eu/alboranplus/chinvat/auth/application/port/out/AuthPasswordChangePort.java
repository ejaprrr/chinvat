package eu.alboranplus.chinvat.auth.application.port.out;

public interface AuthPasswordChangePort {
  void changePassword(Long userId, String rawPassword);
}

