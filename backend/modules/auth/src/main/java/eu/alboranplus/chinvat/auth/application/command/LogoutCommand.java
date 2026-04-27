package eu.alboranplus.chinvat.auth.application.command;

public record LogoutCommand(String accessToken, String refreshToken) {}
