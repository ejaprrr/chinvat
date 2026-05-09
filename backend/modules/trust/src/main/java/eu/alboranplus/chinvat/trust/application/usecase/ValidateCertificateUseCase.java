package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateValidationPort;
import eu.alboranplus.chinvat.trust.application.port.out.TrustedProviderSyncPort;
import org.springframework.stereotype.Service;

@Service
public class ValidateCertificateUseCase {

  private final TrustedProviderSyncPort trustedProviderSyncPort;
  private final CertificateValidationPort certificateValidationPort;

  public ValidateCertificateUseCase(
      TrustedProviderSyncPort trustedProviderSyncPort,
      CertificateValidationPort certificateValidationPort) {
    this.trustedProviderSyncPort = trustedProviderSyncPort;
    this.certificateValidationPort = certificateValidationPort;
  }

  public CertificateValidationResult execute(ValidateCertificateCommand command) {
    if (command.refreshTrustedProvidersBeforeValidation()) {
      trustedProviderSyncPort.synchronize(true);
    }
    return certificateValidationPort.validate(command.certificatePem());
  }
}
