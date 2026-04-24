package eu.alboranplus.chinvat.users.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UsersInfrastructureConfig {

  @Bean("usersPasswordEncoder")
  public PasswordEncoder usersPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
