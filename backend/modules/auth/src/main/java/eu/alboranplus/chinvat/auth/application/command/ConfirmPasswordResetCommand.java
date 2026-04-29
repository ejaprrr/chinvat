package eu.alboranplus.chinvat.auth.application.command;

public record ConfirmPasswordResetCommand(
    String email, String resetCode, String newPassword, String clientIp, String userAgent) {}

