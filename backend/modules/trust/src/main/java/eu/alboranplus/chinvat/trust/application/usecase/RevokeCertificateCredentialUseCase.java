package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeCertificateCredentialUseCase {

  private final CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  public RevokeCertificateCredentialUseCase(
      CertificateCredentialLifecyclePort certificateCredentialLifecyclePort) {
    this.certificateCredentialLifecyclePort = certificateCredentialLifecyclePort;
  }

  @Transactional
  public CertificateCredentialView execute(UUID credentialId, String actor, String reason) {
    CertificateCredentialView existing =
        certificateCredentialLifecyclePort
            .findById(credentialId)
            .orElseThrow(
                () -> new CertificateCredentialNotFoundException(
                    "Certificate credential not found: " + credentialId));
    Instant now = Instant.now();
    CertificateCredentialView revoked =
        new CertificateCredentialView(
            existing.id(),
            existing.userId(),
            existing.providerCode(),
            existing.credentialType(),
            existing.trustStatus(),
            "REVOKED",
            existing.assuranceLevel(),
            existing.registrationSource(),
            existing.certificatePem(),
            existing.thumbprintSha256(),
            existing.subjectDn(),
            existing.issuerDn(),
            existing.serialNumber(),
            existing.notBefore(),
            existing.notAfter(),
            existing.approvedBy(),
            existing.approvedAt(),
            actor,
            now,
            false,
            existing.createdAt(),
            now);
    return certificateCredentialLifecyclePort.save(revoked);
  }
}
