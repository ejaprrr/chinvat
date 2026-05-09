package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetPrimaryCertificateCredentialUseCase {

  private final CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  public SetPrimaryCertificateCredentialUseCase(
      CertificateCredentialLifecyclePort certificateCredentialLifecyclePort) {
    this.certificateCredentialLifecyclePort = certificateCredentialLifecyclePort;
  }

  @Transactional
  public CertificateCredentialView execute(Long userId, Long credentialId) {
    CertificateCredentialView existing =
        certificateCredentialLifecyclePort
            .findById(credentialId)
            .orElseThrow(
                () ->
                    new CertificateCredentialNotFoundException(
                        "Certificate credential not found: " + credentialId));

    if (!existing.userId().equals(userId)) {
      throw new CertificateCredentialNotFoundException(
          "Certificate credential not found for user: " + credentialId);
    }

    if (!"ACTIVE".equals(existing.revocationStatus())) {
      throw new IllegalStateException("Only ACTIVE credentials can be primary");
    }

    Instant now = Instant.now();
    if (existing.notAfter().isBefore(now) || existing.notBefore().isAfter(now)) {
      throw new IllegalStateException("Only currently valid credentials can be primary");
    }

    certificateCredentialLifecyclePort.clearPrimaryForUser(userId);

    CertificateCredentialView primary =
        new CertificateCredentialView(
            existing.id(),
            existing.userId(),
            existing.providerCode(),
            existing.credentialType(),
            existing.trustStatus(),
            existing.revocationStatus(),
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
            existing.revokedBy(),
            existing.revokedAt(),
            true,
            existing.createdAt(),
            now);

    return certificateCredentialLifecyclePort.save(primary);
  }
}
