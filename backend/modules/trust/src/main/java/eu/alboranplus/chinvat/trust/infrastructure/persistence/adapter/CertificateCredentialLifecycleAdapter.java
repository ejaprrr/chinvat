package eu.alboranplus.chinvat.trust.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.infrastructure.persistence.jpa.CertificateCredentialJpaRepository;
import eu.alboranplus.chinvat.trust.infrastructure.persistence.mapper.CertificateCredentialJpaMapper;
import java.util.List;
import java.util.Optional;
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
  public List<CertificateCredentialView> findAll(Long userId) {
    return (userId == null
            ? certificateCredentialJpaRepository.findAllByOrderByCreatedAtDesc()
            : certificateCredentialJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
        .stream()
        .map(certificateCredentialJpaMapper::toView)
        .toList();
  }

  @Override
  public Optional<CertificateCredentialView> findById(Long credentialId) {
    return certificateCredentialJpaRepository.findById(credentialId)
        .map(certificateCredentialJpaMapper::toView);
  }

  @Override
  public void clearPrimaryForUser(Long userId) {
    certificateCredentialJpaRepository.clearPrimaryForUser(userId);
  }
}
