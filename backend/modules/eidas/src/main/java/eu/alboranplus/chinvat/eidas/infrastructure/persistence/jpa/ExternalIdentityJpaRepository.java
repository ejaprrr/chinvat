package eu.alboranplus.chinvat.eidas.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.eidas.infrastructure.persistence.entity.ExternalIdentityJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalIdentityJpaRepository extends JpaRepository<ExternalIdentityJpaEntity, Long> {

  Optional<ExternalIdentityJpaEntity>
      findFirstByProviderCodeAndExternalSubjectIdAndUserIdIsNotNullOrderByCreatedAtDesc(
          String providerCode, String externalSubjectId);

  Optional<ExternalIdentityJpaEntity>
      findFirstByProviderCodeAndExternalSubjectIdOrderByCreatedAtDesc(
          String providerCode, String externalSubjectId);
}