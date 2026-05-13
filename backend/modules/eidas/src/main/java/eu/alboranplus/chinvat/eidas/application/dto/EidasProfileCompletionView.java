package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EidasProfileCompletionView(
    String providerCode,
    String externalSubjectId,
    UUID userId,
    String currentStatus,
    Instant linkedAt,
    Instant updatedAt) {}
