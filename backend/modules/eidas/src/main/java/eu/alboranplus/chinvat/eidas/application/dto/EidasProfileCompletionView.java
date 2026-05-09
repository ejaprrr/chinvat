package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;

public record EidasProfileCompletionView(
    String providerCode,
    String externalSubjectId,
    Long userId,
    String currentStatus,
    Instant linkedAt,
    Instant updatedAt) {}
