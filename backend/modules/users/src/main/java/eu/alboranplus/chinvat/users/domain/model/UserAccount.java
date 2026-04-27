package eu.alboranplus.chinvat.users.domain.model;

import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;

public record UserAccount(
    Long id,
    String username,
    String fullName,
    String phoneNumber,
    UserEmail email,
    UserType userType,
    AccessLevel accessLevel,
    String addressLine,
    String postalCode,
    String city,
    String country,
    String defaultLanguage,
    Instant createdAt,
    Instant updatedAt) {

  public UserAccount {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }
    if (fullName == null || fullName.isBlank()) {
      throw new IllegalArgumentException("Full name must not be blank");
    }
    if (email == null) {
      throw new IllegalArgumentException("Email must not be null");
    }
    if (userType == null) {
      throw new IllegalArgumentException("User type must not be null");
    }
    if (accessLevel == null) {
      throw new IllegalArgumentException("Access level must not be null");
    }
    if (defaultLanguage == null || defaultLanguage.isBlank()) {
      throw new IllegalArgumentException("Default language must not be blank");
    }
    username = username.trim();
    fullName = fullName.trim();
  }

  public static UserAccount newUser(
      String username,
      String fullName,
      String phoneNumber,
      UserEmail email,
      UserType userType,
      AccessLevel accessLevel,
      String addressLine,
      String postalCode,
      String city,
      String country,
      String defaultLanguage,
      Instant now) {
    return new UserAccount(
        null, username, fullName, phoneNumber, email, userType, accessLevel,
        addressLine, postalCode, city, country, defaultLanguage, now, now);
  }

  public UserAccount withUpdatedFields(
      String username,
      String fullName,
      String phoneNumber,
      UserType userType,
      AccessLevel accessLevel,
      String addressLine,
      String postalCode,
      String city,
      String country,
      String defaultLanguage,
      Instant updatedAt) {
    return new UserAccount(
        this.id, username, fullName, phoneNumber, this.email, userType, accessLevel,
        addressLine, postalCode, city, country, defaultLanguage, this.createdAt, updatedAt);
  }
}
