package eu.alboranplus.chinvat.users.application.command;

import java.util.Set;

public record CreateUserCommand(
    String email, String rawPassword, String displayName, Set<String> roles) {

  public CreateUserCommand {
    roles = roles == null ? Set.of() : Set.copyOf(roles);
  }
}
