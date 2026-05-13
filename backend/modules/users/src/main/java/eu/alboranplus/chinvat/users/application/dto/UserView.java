package eu.alboranplus.chinvat.users.application.dto;

import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import java.util.UUID;

public record UserView(
    UUID id,
    String username,
    String fullName,
    String phoneNumber,
    String email,
    UserType userType,
    AccessLevel accessLevel,
    String addressLine,
    String postalCode,
    String city,
    String country,
    String defaultLanguage) {}

