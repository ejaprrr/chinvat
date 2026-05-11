package eu.alboranplus.chinvat.trust.application.facade;

import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.command.BindCertificateCredentialCommand;
import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import java.util.List;
import java.util.UUID;

public interface TrustFacade {
  CertificateValidationResult validateCertificate(ValidateCertificateCommand command);

  TrustedProviderSyncResult syncTrustedProviders(SyncTrustedProvidersCommand command);

  CertificateCredentialView bindCertificateCredential(BindCertificateCredentialCommand command, String actor);

  List<CertificateCredentialView> listCertificateCredentials(UUID userId);

  PageResponse<CertificateCredentialView> listCertificateCredentialsPaged(UUID userId, PaginationRequest paginationRequest);

  void revokeCertificateCredential(UUID credentialId, String actor, String reason);

  CertificateCredentialView setPrimaryCertificateCredential(UUID userId, UUID credentialId, String actor);
}
