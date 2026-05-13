package eu.alboranplus.chinvat.eidas.api.dto;

import java.time.Instant;
import java.util.UUID;

public record EidasCallbackResponse(
    String providerCode,
    String externalSubjectId,
    String levelOfAssurance,
    String currentStatus,
    UUID linkedUserId,
    boolean profileCompletionRequired,
    Instant processedAt) {}
