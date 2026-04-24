package eu.alboranplus.chinvat.users.infrastructure.crypto;

import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsersPasswordHasherAdapter implements UsersPasswordHasherPort {

  private final PasswordEncoder usersPasswordEncoder;

  public UsersPasswordHasherAdapter(
      @Qualifier("usersPasswordEncoder") PasswordEncoder usersPasswordEncoder) {
    this.usersPasswordEncoder = usersPasswordEncoder;
  }

  @Override
  public String hash(String rawPassword) {
    return usersPasswordEncoder.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String passwordHash) {
    return usersPasswordEncoder.matches(rawPassword, passwordHash);
  }
}
