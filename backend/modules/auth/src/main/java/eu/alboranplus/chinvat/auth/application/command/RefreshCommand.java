package eu.alboranplus.chinvat.auth.application.command;

public record RefreshCommand(String refreshToken, String clientIp, String userAgent) {}
