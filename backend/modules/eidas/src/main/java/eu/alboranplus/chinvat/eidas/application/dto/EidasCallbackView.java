package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EidasCallbackView(
    String providerCode,
    String externalSubjectId,
    String levelOfAssurance,
    String currentStatus,
    UUID linkedUserId,
    boolean profileCompletionRequired,
    Instant processedAt) {}
