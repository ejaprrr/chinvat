package eu.alboranplus.chinvat.eidas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "eIDAS callback payload returned from the identity broker")
public record EidasCallbackRequest(
    @Schema(description = "Provider code", example = "EIDAS_EU")
        @NotBlank(message = "providerCode is required") String providerCode,
    @Schema(description = "State token returned by the broker — must match the one sent in the login request",
        example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        @NotBlank(message = "state is required") String state,
    @Schema(description = "Authorization code returned by the eIDAS broker", example = "eyJhbGciOiJSUzI1NiJ9...")
        @NotBlank(message = "authorizationCode is required") String authorizationCode,
    @Schema(description = "External subject identifier assigned by the eIDAS node", example = "ES/ES/12345678A")
        @NotBlank(message = "externalSubjectId is required") String externalSubjectId,
    @Schema(description = "Level of assurance of the authentication",
        example = "http://eidas.europa.eu/LoA/high")
        @NotBlank(message = "levelOfAssurance is required") String levelOfAssurance) {}
