package eu.alboranplus.chinvat.auth.application.command;

public record CertificateLoginCommand(String thumbprintSha256, String clientIp, String userAgent) {}