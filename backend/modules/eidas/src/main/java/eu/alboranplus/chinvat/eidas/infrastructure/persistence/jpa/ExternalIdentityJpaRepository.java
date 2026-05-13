package eu.alboranplus.chinvat.eidas.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.eidas.infrastructure.persistence.entity.ExternalIdentityJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExternalIdentityJpaRepository extends JpaRepository<ExternalIdentityJpaEntity, UUID> {

  Optional<ExternalIdentityJpaEntity>
      findFirstByProviderCodeAndExternalSubjectIdAndUserIdIsNotNullOrderByCreatedAtDesc(
          String providerCode, String externalSubjectId);

  Optional<ExternalIdentityJpaEntity>
      findFirstByProviderCodeAndExternalSubjectIdOrderByCreatedAtDesc(
          String providerCode, String externalSubjectId);
}