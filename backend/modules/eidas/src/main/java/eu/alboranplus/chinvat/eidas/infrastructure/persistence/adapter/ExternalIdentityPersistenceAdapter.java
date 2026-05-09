package eu.alboranplus.chinvat.eidas.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import eu.alboranplus.chinvat.eidas.application.port.out.ExternalIdentityLifecyclePort;
import eu.alboranplus.chinvat.eidas.infrastructure.persistence.entity.ExternalIdentityJpaEntity;
import eu.alboranplus.chinvat.eidas.infrastructure.persistence.jpa.ExternalIdentityJpaRepository;
import eu.alboranplus.chinvat.eidas.infrastructure.persistence.mapper.ExternalIdentityJpaMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalIdentityPersistenceAdapter implements ExternalIdentityLifecyclePort {

  private static final String DEFAULT_IDENTITY_SOURCE = "EIDAS_BROKER";

  private final ExternalIdentityJpaRepository externalIdentityJpaRepository;
  private final ExternalIdentityJpaMapper externalIdentityJpaMapper;

  public ExternalIdentityPersistenceAdapter(
      ExternalIdentityJpaRepository externalIdentityJpaRepository,
      ExternalIdentityJpaMapper externalIdentityJpaMapper) {
    this.externalIdentityJpaRepository = externalIdentityJpaRepository;
    this.externalIdentityJpaMapper = externalIdentityJpaMapper;
  }

  @Override
  public Optional<Long> findLinkedUserId(String providerCode, String externalSubjectId) {
    return externalIdentityJpaRepository
        .findFirstByProviderCodeAndExternalSubjectIdAndUserIdIsNotNullOrderByCreatedAtDesc(
            providerCode, externalSubjectId)
        .map(ExternalIdentityJpaEntity::getUserId);
  }

  @Override
  public Optional<ExternalIdentityView> findLatest(String providerCode, String externalSubjectId) {
    return externalIdentityJpaRepository
        .findFirstByProviderCodeAndExternalSubjectIdOrderByCreatedAtDesc(
            providerCode, externalSubjectId)
        .map(externalIdentityJpaMapper::toView);
  }

  @Override
  public ExternalIdentityView save(ExternalIdentityView identity) {
    ExternalIdentityJpaEntity entity =
        externalIdentityJpaRepository
            .findFirstByProviderCodeAndExternalSubjectIdOrderByCreatedAtDesc(
                identity.providerCode(), identity.externalSubjectId())
            .map(existing -> merge(existing, identity))
            .orElseGet(() -> externalIdentityJpaMapper.toEntity(identity));

    if (entity.getIdentitySource() == null || entity.getIdentitySource().isBlank()) {
      entity.setIdentitySource(DEFAULT_IDENTITY_SOURCE);
    }
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(Instant.now());
    }
    entity.setUpdatedAt(identity.updatedAt());

    return externalIdentityJpaMapper.toView(externalIdentityJpaRepository.save(entity));
  }

  private static ExternalIdentityJpaEntity merge(
      ExternalIdentityJpaEntity entity, ExternalIdentityView view) {
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
    entity.setUpdatedAt(view.updatedAt());
    return entity;
  }
}