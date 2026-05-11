package eu.alboranplus.chinvat.trust.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.trust.application.command.BindCertificateCredentialCommand;
import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import eu.alboranplus.chinvat.trust.application.usecase.BindCertificateCredentialUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.ListCertificateCredentialsUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.ListCertificateCredentialsPagedUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.RevokeCertificateCredentialUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.SetPrimaryCertificateCredentialUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.SyncTrustedProvidersUseCase;
import eu.alboranplus.chinvat.trust.application.usecase.ValidateCertificateUseCase;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TrustFacadeService implements TrustFacade {

  private final ValidateCertificateUseCase validateCertificateUseCase;
  private final SyncTrustedProvidersUseCase syncTrustedProvidersUseCase;
  private final BindCertificateCredentialUseCase bindCertificateCredentialUseCase;
  private final ListCertificateCredentialsUseCase listCertificateCredentialsUseCase;
  private final ListCertificateCredentialsPagedUseCase listCertificateCredentialsPagedUseCase;
  private final RevokeCertificateCredentialUseCase revokeCertificateCredentialUseCase;
  private final SetPrimaryCertificateCredentialUseCase setPrimaryCertificateCredentialUseCase;
  private final AuditFacade auditFacade;

  public TrustFacadeService(
      ValidateCertificateUseCase validateCertificateUseCase,
      SyncTrustedProvidersUseCase syncTrustedProvidersUseCase,
      BindCertificateCredentialUseCase bindCertificateCredentialUseCase,
      ListCertificateCredentialsUseCase listCertificateCredentialsUseCase,
      ListCertificateCredentialsPagedUseCase listCertificateCredentialsPagedUseCase,
      RevokeCertificateCredentialUseCase revokeCertificateCredentialUseCase,
      SetPrimaryCertificateCredentialUseCase setPrimaryCertificateCredentialUseCase,
      AuditFacade auditFacade) {
    this.validateCertificateUseCase = validateCertificateUseCase;
    this.syncTrustedProvidersUseCase = syncTrustedProvidersUseCase;
    this.bindCertificateCredentialUseCase = bindCertificateCredentialUseCase;
    this.listCertificateCredentialsUseCase = listCertificateCredentialsUseCase;
    this.listCertificateCredentialsPagedUseCase = listCertificateCredentialsPagedUseCase;
    this.revokeCertificateCredentialUseCase = revokeCertificateCredentialUseCase;
    this.setPrimaryCertificateCredentialUseCase = setPrimaryCertificateCredentialUseCase;
    this.auditFacade = auditFacade;
  }

  @Override
  public CertificateValidationResult validateCertificate(ValidateCertificateCommand command) {
    return validateCertificateUseCase.execute(command);
  }

  @Override
  public TrustedProviderSyncResult syncTrustedProviders(SyncTrustedProvidersCommand command) {
    return syncTrustedProvidersUseCase.execute(command);
  }

  @Override
  public CertificateCredentialView bindCertificateCredential(
      BindCertificateCredentialCommand command, String actor) {
    CertificateCredentialView created = bindCertificateCredentialUseCase.execute(command, actor);
    auditFacade.log(
        "TRUST_CREDENTIAL_BOUND",
        actor,
        created.userId(),
        AuditDetails.builder()
            .add("credentialId", created.id())
            .add("providerCode", created.providerCode())
            .add("thumbprintSha256", created.thumbprintSha256())
            .add("credentialType", created.credentialType())
            .build());
    return created;
  }

  @Override
  public List<CertificateCredentialView> listCertificateCredentials(UUID userId) {
    return listCertificateCredentialsUseCase.execute(userId);
  }

  @Override
  public void revokeCertificateCredential(UUID credentialId, String actor, String reason) {
    CertificateCredentialView revoked = revokeCertificateCredentialUseCase.execute(credentialId, actor, reason);
    auditFacade.log(
        "TRUST_CREDENTIAL_REVOKED",
        actor,
        revoked.userId(),
        AuditDetails.builder()
            .add("credentialId", revoked.id())
            .add("providerCode", revoked.providerCode())
            .add("thumbprintSha256", revoked.thumbprintSha256())
            .add("reason", reason)
            .build());
  }

  @Override
  public CertificateCredentialView setPrimaryCertificateCredential(
      UUID userId, UUID credentialId, String actor) {
    CertificateCredentialView primary =
        setPrimaryCertificateCredentialUseCase.execute(userId, credentialId);
    auditFacade.log(
        "TRUST_CREDENTIAL_PRIMARY_SET",
        actor,
        userId,
        AuditDetails.builder()
            .add("credentialId", primary.id())
            .add("thumbprintSha256", primary.thumbprintSha256())
            .build());
    return primary;
  }

  @Override
  public PageResponse<CertificateCredentialView> listCertificateCredentialsPaged(
      UUID userId, PaginationRequest paginationRequest) {
    return listCertificateCredentialsPagedUseCase.execute(userId, paginationRequest);
  }
}
