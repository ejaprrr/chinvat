package eu.alboranplus.chinvat.eidas.infrastructure.persistence.mapper;

import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import eu.alboranplus.chinvat.eidas.infrastructure.persistence.entity.ExternalIdentityJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExternalIdentityJpaMapper {

  public ExternalIdentityJpaEntity toEntity(ExternalIdentityView view) {
    ExternalIdentityJpaEntity entity = new ExternalIdentityJpaEntity();
    entity.setId(view.id());
    entity.setUserId(view.userId());
    entity.setProviderCode(view.providerCode());
    entity.setIdentitySource(view.identitySource());
    entity.setExternalSubjectId(view.externalSubjectId());
    entity.setAssuranceLevel(view.assuranceLevel());
    entity.setPersonIdentifier(view.personIdentifier());
    entity.setLegalPersonIdentifier(view.legalPersonIdentifier());
    entity.setIdentityReference(view.identityReference());
    entity.setNationality(view.nationality());
    entity.setFirstName(view.firstName());
    entity.setFamilyName(view.familyName());
    entity.setDateOfBirth(view.dateOfBirth());
    entity.setRawClaimsJson(view.rawClaimsJson());
    entity.setCurrentStatus(view.currentStatus());
    entity.setReviewedBy(view.reviewedBy());
    entity.setReviewedAt(view.reviewedAt());
    entity.setReviewReason(view.reviewReason());
    entity.setLinkedAt(view.linkedAt());
    entity.setUnlinkedAt(view.unlinkedAt());
    entity.setCreatedAt(view.createdAt());
    entity.setUpdatedAt(view.updatedAt());
    return entity;
  }

  public ExternalIdentityView toView(ExternalIdentityJpaEntity entity) {
    return new ExternalIdentityView(
        entity.getId(),
        entity.getUserId(),
        entity.getProviderCode(),
        entity.getIdentitySource(),
        entity.getExternalSubjectId(),
        entity.getAssuranceLevel(),
        entity.getPersonIdentifier(),
        entity.getLegalPersonIdentifier(),
        entity.getIdentityReference(),
        entity.getNationality(),
        entity.getFirstName(),
        entity.getFamilyName(),
        entity.getDateOfBirth(),
        entity.getRawClaimsJson(),
        entity.getCurrentStatus(),
        entity.getReviewedBy(),
        entity.getReviewedAt(),
        entity.getReviewReason(),
        entity.getLinkedAt(),
        entity.getUnlinkedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}