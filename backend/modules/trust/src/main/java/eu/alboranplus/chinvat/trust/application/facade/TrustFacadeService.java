package eu.alboranplus.chinvat.trust.application.facade;

import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import eu.alboranplus.chinvat.trust.application.usecase.SyncTrustedProvidersUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.ValidateCertificateUseCase;
import org.springframework.stereotype.Service;

@Service
public class TrustFacadeService implements TrustFacade {

  private final ValidateCertificateUseCase validateCertificateUseCase;
  private final SyncTrustedProvidersUseCase syncTrustedProvidersUseCase;

  public TrustFacadeService(
      ValidateCertificateUseCase validateCertificateUseCase,
      SyncTrustedProvidersUseCase syncTrustedProvidersUseCase) {
    this.validateCertificateUseCase = validateCertificateUseCase;
    this.syncTrustedProvidersUseCase = syncTrustedProvidersUseCase;
  }

  @Override
  public CertificateValidationResult validateCertificate(ValidateCertificateCommand command) {
    return validateCertificateUseCase.execute(command);
  }

  @Override
  public TrustedProviderSyncResult syncTrustedProviders(SyncTrustedProvidersCommand command) {
    return syncTrustedProvidersUseCase.execute(command);
  }
}
