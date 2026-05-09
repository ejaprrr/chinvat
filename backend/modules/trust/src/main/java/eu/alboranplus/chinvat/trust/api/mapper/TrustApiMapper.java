package eu.alboranplus.chinvat.trust.api.mapper;

import eu.alboranplus.chinvat.trust.api.dto.SyncTrustedProvidersRequest;
import eu.alboranplus.chinvat.trust.api.dto.SyncTrustedProvidersResponse;
import eu.alboranplus.chinvat.trust.api.dto.ValidateCertificateRequest;
import eu.alboranplus.chinvat.trust.api.dto.ValidateCertificateResponse;
import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.command.ValidateCertificateCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import org.springframework.stereotype.Component;

@Component
public class TrustApiMapper {

  public ValidateCertificateCommand toCommand(ValidateCertificateRequest request) {
    return new ValidateCertificateCommand(
        request.certificatePem(), request.refreshTrustedProvidersBeforeValidation());
  }

  public ValidateCertificateResponse toResponse(CertificateValidationResult result) {
    return new ValidateCertificateResponse(
        result.thumbprintSha256(),
        result.subjectDn(),
        result.issuerDn(),
        result.serialNumber(),
        result.notBefore(),
        result.notAfter(),
        result.validNow(),
        result.trustedIssuer(),
        result.trustSource(),
        result.keyUsageFlags(),
        result.validatedAt());
  }

  public SyncTrustedProvidersCommand toCommand(SyncTrustedProvidersRequest request) {
    boolean onlineRefresh = request == null || request.onlineRefresh() == null || request.onlineRefresh();
    return new SyncTrustedProvidersCommand(onlineRefresh);
  }

  public SyncTrustedProvidersResponse toResponse(TrustedProviderSyncResult result) {
    return new SyncTrustedProvidersResponse(
        result.sourceUrl(),
        result.processedLotlCount(),
        result.processedTlCount(),
        result.trustedCertificates(),
        result.synchronizedAt());
  }
}
