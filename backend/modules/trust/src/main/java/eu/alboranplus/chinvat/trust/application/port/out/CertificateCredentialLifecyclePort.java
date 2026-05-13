package eu.alboranplus.chinvat.trust.application.port.out;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CertificateCredentialLifecyclePort {
  CertificateCredentialView save(CertificateCredentialView credential);

  List<CertificateCredentialView> findAll(UUID userId);

  Page<CertificateCredentialView> findAllPaged(UUID userId, Pageable pageable);

  Optional<CertificateCredentialView> findById(UUID credentialId);

  void clearPrimaryForUser(UUID userId);
}
