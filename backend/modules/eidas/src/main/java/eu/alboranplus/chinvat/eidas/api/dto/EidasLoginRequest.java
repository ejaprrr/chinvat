package eu.alboranplus.chinvat.eidas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EidasLoginRequest(
    @NotBlank(message = "providerCode is required") String providerCode,
    @NotBlank(message = "redirectUri is required") String redirectUri) {}
