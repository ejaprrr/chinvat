package eu.alboranplus.chinvat.trust.application.port.out;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CertificateCredentialLifecyclePort {
  CertificateCredentialView save(CertificateCredentialView credential);

  List<CertificateCredentialView> findAll(Long userId);

  Page<CertificateCredentialView> findAllPaged(Long userId, Pageable pageable);

  Optional<CertificateCredentialView> findById(Long credentialId);

  void clearPrimaryForUser(Long userId);
}
