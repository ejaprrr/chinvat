package eu.alboranplus.chinvat.eidas.application.dto;

import java.time.Instant;

public record ExternalIdentityView(
    Long id,
    Long userId,
    String providerCode,
    String identitySource,
    String externalSubjectId,
    String assuranceLevel,
    String personIdentifier,
    String legalPersonIdentifier,
    String identityReference,
    String nationality,
    String firstName,
    String familyName,
    String dateOfBirth,
    String rawClaimsJson,
    String currentStatus,
    String reviewedBy,
    Instant reviewedAt,
    String reviewReason,
    Instant linkedAt,
    Instant unlinkedAt,
    Instant createdAt,
        Instant updatedAt) {

    public ExternalIdentityView withLinkedUser(
            Long linkedUserId,
            String newStatus,
            String newIdentityReference,
            String newNationality,
            Instant now) {
        return new ExternalIdentityView(
                id,
                linkedUserId,
                providerCode,
                identitySource,
                externalSubjectId,
                assuranceLevel,
                personIdentifier,
                legalPersonIdentifier,
                newIdentityReference == null ? identityReference : newIdentityReference,
                newNationality == null ? nationality : newNationality,
                firstName,
                familyName,
                dateOfBirth,
                rawClaimsJson,
                newStatus,
                reviewedBy,
                reviewedAt,
                reviewReason,
                now,
                null,
                createdAt,
                now);
    }
}