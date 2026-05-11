package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PaginationUtils;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCertificateCredentialsPagedUseCase {

  private final CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  public ListCertificateCredentialsPagedUseCase(
      CertificateCredentialLifecyclePort certificateCredentialLifecyclePort) {
    this.certificateCredentialLifecyclePort = certificateCredentialLifecyclePort;
  }

  @Transactional(readOnly = true)
  public PageResponse<CertificateCredentialView> execute(
      Long userId, PaginationRequest paginationRequest) {
    Page<CertificateCredentialView> page =
        certificateCredentialLifecyclePort.findAllPaged(userId, paginationRequest.toPageable());
    return PaginationUtils.toPageResponse(page);
  }
}
