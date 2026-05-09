package eu.alboranplus.chinvat.eidas.api.dto;

import java.time.Instant;

public record EidasCallbackResponse(
    String providerCode,
    String externalSubjectId,
    String levelOfAssurance,
    String currentStatus,
    Long linkedUserId,
    boolean profileCompletionRequired,
    Instant processedAt) {}
