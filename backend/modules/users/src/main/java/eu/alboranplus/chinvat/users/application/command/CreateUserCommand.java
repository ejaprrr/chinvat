package eu.alboranplus.chinvat.users.application.command;

import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;

public record CreateUserCommand(
    String username,
    String fullName,
    String phoneNumber,
    String email,
    String rawPassword,
    UserType userType,
    AccessLevel accessLevel,
    String addressLine,
    String postalCode,
    String city,
    String country,
    String defaultLanguage) {}

