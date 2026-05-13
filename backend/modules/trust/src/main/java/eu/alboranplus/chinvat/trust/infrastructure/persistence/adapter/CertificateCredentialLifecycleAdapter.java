package eu.alboranplus.chinvat.trust.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.infrastructure.persistence.jpa.CertificateCredentialJpaRepository;
import eu.alboranplus.chinvat.trust.infrastructure.persistence.mapper.CertificateCredentialJpaMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class CertificateCredentialLifecycleAdapter implements CertificateCredentialLifecyclePort {

  private final CertificateCredentialJpaRepository certificateCredentialJpaRepository;
  private final CertificateCredentialJpaMapper certificateCredentialJpaMapper;

  public CertificateCredentialLifecycleAdapter(
      CertificateCredentialJpaRepository certificateCredentialJpaRepository,
      CertificateCredentialJpaMapper certificateCredentialJpaMapper) {
    this.certificateCredentialJpaRepository = certificateCredentialJpaRepository;
    this.certificateCredentialJpaMapper = certificateCredentialJpaMapper;
  }

  @Override
  public CertificateCredentialView save(CertificateCredentialView credential) {
    var entity = certificateCredentialJpaMapper.toEntity(credential);
    var persisted = certificateCredentialJpaRepository.save(entity);
    return certificateCredentialJpaMapper.toView(persisted);
  }

  @Override
  public List<CertificateCredentialView> findAll(UUID userId) {
    return (userId == null
            ? certificateCredentialJpaRepository.findAllByOrderByCreatedAtDesc()
            : certificateCredentialJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
        .stream()
        .map(certificateCredentialJpaMapper::toView)
        .toList();
  }

  @Override
  public Page<CertificateCredentialView> findAllPaged(UUID userId, Pageable pageable) {
    Page<eu.alboranplus.chinvat.trust.infrastructure.persistence.entity.CertificateCredentialJpaEntity> page =
        userId == null
            ? certificateCredentialJpaRepository.findAll(pageable)
            : certificateCredentialJpaRepository.findAllByUserId(userId, pageable);
    return page.map(certificateCredentialJpaMapper::toView);
  }

  @Override
  public Optional<CertificateCredentialView> findById(UUID credentialId) {
    return certificateCredentialJpaRepository.findById(credentialId)
        .map(certificateCredentialJpaMapper::toView);
  }

  @Override
  public void clearPrimaryForUser(UUID userId) {
    certificateCredentialJpaRepository.clearPrimaryForUser(userId);
  }
}
