package eu.alboranplus.chinvat.users.application.command;

public record ChangePasswordCommand(Long userId, String rawPassword) {}

