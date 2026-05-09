package eu.alboranplus.chinvat.trust.application.facade;

import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;

public interface TrustFacade {
  CertificateValidationResult validateCertificate(ValidateCertificateCommand command);

  TrustedProviderSyncResult syncTrustedProviders(SyncTrustedProvidersCommand command);
}
