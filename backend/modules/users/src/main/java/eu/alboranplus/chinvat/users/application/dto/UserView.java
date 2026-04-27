package eu.alboranplus.chinvat.users.application.dto;

import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;

public record UserView(
    Long id,
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

