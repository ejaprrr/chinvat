package eu.alboranplus.chinvat.auth.application.command;

public record RequestPasswordResetCommand(
    String email, String clientIp, String userAgent, boolean revealCode) {}

