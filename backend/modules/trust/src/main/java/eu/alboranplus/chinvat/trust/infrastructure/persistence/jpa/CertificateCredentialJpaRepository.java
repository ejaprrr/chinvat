package eu.alboranplus.chinvat.trust.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.trust.infrastructure.persistence.entity.CertificateCredentialJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CertificateCredentialJpaRepository
    extends JpaRepository<CertificateCredentialJpaEntity, Long> {

  List<CertificateCredentialJpaEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);

  List<CertificateCredentialJpaEntity> findAllByOrderByCreatedAtDesc();

  @Modifying
  @Query(
      """
      update CertificateCredentialJpaEntity credential
      set credential.primary = false
      where credential.userId = :userId
      """)
  void clearPrimaryForUser(@Param("userId") Long userId);
}
