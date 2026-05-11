package eu.alboranplus.chinvat.profile.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CompleteEidasProfileResponse(
    UUID userId,
    String providerCode,
    String externalSubjectId,
    String currentStatus,
    Instant linkedAt,
    Instant completedAt) {}
