package eu.alboranplus.chinvat.users.application.port.out;

public interface UsersPasswordHasherPort {
  String hash(String rawPassword);

  boolean matches(String rawPassword, String passwordHash);
}
