package eu.alboranplus.chinvat.eidas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "eIDAS login initiation payload")
public record EidasLoginRequest(
    @Schema(description = "Provider code identifying the eIDAS node", example = "EIDAS_EU")
        @NotBlank(message = "providerCode is required") String providerCode,
    @Schema(description = "Frontend redirect URI to return to after authentication",
        example = "https://app.example.com/auth/eidas/callback")
        @NotBlank(message = "redirectUri is required") String redirectUri) {}
