package eu.alboranplus.chinvat.eidas.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EidasCallbackRequest(
    @NotBlank(message = "providerCode is required") String providerCode,
    @NotBlank(message = "state is required") String state,
    @NotBlank(message = "authorizationCode is required") String authorizationCode,
    @NotBlank(message = "externalSubjectId is required") String externalSubjectId,
    @NotBlank(message = "levelOfAssurance is required") String levelOfAssurance) {}
