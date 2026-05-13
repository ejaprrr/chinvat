package eu.alboranplus.chinvat.trust.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.trust.infrastructure.persistence.entity.CertificateCredentialJpaEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CertificateCredentialJpaRepository
    extends JpaRepository<CertificateCredentialJpaEntity, UUID> {

  List<CertificateCredentialJpaEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  Page<CertificateCredentialJpaEntity> findAllByUserId(UUID userId, Pageable pageable);

  List<CertificateCredentialJpaEntity> findAllByOrderByCreatedAtDesc();

  Page<CertificateCredentialJpaEntity> findAll(Pageable pageable);

  @Modifying
  @Query(
      """
      update CertificateCredentialJpaEntity credential
      set credential.primary = false
      where credential.userId = :userId
      """)
  void clearPrimaryForUser(@Param("userId") UUID userId);
}
