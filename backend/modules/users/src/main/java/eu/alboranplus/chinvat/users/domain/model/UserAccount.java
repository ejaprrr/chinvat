package eu.alboranplus.chinvat.users.domain.model;

import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Set;

public record UserAccount(
    Long id,
    UserEmail email,
    String displayName,
    String passwordHash,
    Set<String> roles,
    boolean active,
    Instant createdAt) {

  public UserAccount {
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("Display name must not be blank");
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash must not be blank");
    }

    roles = Set.copyOf(roles);
  }

  public static UserAccount newUser(
      UserEmail email,
      String displayName,
      String passwordHash,
      Set<String> roles,
      Instant createdAt) {
    return new UserAccount(null, email, displayName.trim(), passwordHash, roles, true, createdAt);
  }
}
