package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListCertificateCredentialsUseCase {

  private final CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  public ListCertificateCredentialsUseCase(
      CertificateCredentialLifecyclePort certificateCredentialLifecyclePort) {
    this.certificateCredentialLifecyclePort = certificateCredentialLifecyclePort;
  }

  public List<CertificateCredentialView> execute(UUID userId) {
    return certificateCredentialLifecyclePort.findAll(userId);
  }
}
