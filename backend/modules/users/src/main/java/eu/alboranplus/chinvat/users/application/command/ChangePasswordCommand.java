package eu.alboranplus.chinvat.users.application.command;

import java.util.UUID;

public record ChangePasswordCommand(UUID userId, String rawPassword) {}

