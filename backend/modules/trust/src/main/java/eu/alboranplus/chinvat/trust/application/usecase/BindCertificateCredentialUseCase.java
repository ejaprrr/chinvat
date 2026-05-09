package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.command.BindCertificateCredentialCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateValidationPort;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BindCertificateCredentialUseCase {

  private final CertificateValidationPort certificateValidationPort;
  private final CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  public BindCertificateCredentialUseCase(
      CertificateValidationPort certificateValidationPort,
      CertificateCredentialLifecyclePort certificateCredentialLifecyclePort) {
    this.certificateValidationPort = certificateValidationPort;
    this.certificateCredentialLifecyclePort = certificateCredentialLifecyclePort;
  }

  @Transactional
  public CertificateCredentialView execute(BindCertificateCredentialCommand command, String actor) {
    var validationResult = certificateValidationPort.validate(command.certificatePem());
    Instant now = Instant.now();
    CertificateCredentialView candidate =
        new CertificateCredentialView(
            null,
            command.userId(),
            command.providerCode(),
            "CLIENT_TLS",
            validationResult.trustedIssuer() ? "TRUSTED" : "UNTRUSTED",
            "ACTIVE",
            command.assuranceLevel(),
            command.registrationSource(),
            command.certificatePem(),
            validationResult.thumbprintSha256(),
            validationResult.subjectDn(),
            validationResult.issuerDn(),
            validationResult.serialNumber(),
            validationResult.notBefore(),
            validationResult.notAfter(),
            actor,
            now,
            null,
            null,
            false,
            now,
            now);
    return certificateCredentialLifecyclePort.save(candidate);
  }
}
