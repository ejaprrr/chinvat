package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;

public record EidasCallbackView(
    String providerCode,
    String externalSubjectId,
    String levelOfAssurance,
    String currentStatus,
    Long linkedUserId,
    boolean profileCompletionRequired,
    Instant processedAt) {}
