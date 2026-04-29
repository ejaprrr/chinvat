package eu.alboranplus.chinvat.auth.application.command;

import eu.alboranplus.chinvat.users.domain.model.UserType;

public record RegisterCommand(
    String username,
    String fullName,
    String phoneNumber,
    String email,
    String rawPassword,
    UserType userType,
    String addressLine,
    String postalCode,
    String city,
    String country,
    String defaultLanguage,
    String clientIp,
    String userAgent) {}

