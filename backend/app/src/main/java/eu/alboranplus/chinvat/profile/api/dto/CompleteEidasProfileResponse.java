package eu.alboranplus.chinvat.profile.api.dto;

import java.time.Instant;

public record CompleteEidasProfileResponse(
    Long userId,
    String providerCode,
    String externalSubjectId,
    String currentStatus,
    Instant linkedAt,
    Instant completedAt) {}
