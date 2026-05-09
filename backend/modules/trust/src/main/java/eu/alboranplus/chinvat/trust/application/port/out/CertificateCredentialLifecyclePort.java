package eu.alboranplus.chinvat.trust.application.port.out;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import java.util.List;
import java.util.Optional;

public interface CertificateCredentialLifecyclePort {
  CertificateCredentialView save(CertificateCredentialView credential);

  List<CertificateCredentialView> findAll(Long userId);

  Optional<CertificateCredentialView> findById(Long credentialId);

  void clearPrimaryForUser(Long userId);
}
