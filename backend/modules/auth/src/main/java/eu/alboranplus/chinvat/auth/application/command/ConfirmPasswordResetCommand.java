package eu.alboranplus.chinvat.auth.application.command;

public record ConfirmPasswordResetCommand(
    String resetToken, String newPassword, String clientIp, String userAgent) {}

