package eu.alboranplus.chinvat.auth.application.command;

public record ChangePasswordCommand(String currentPassword, String newPassword) {}