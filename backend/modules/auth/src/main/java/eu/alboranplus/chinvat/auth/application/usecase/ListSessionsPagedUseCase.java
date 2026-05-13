package eu.alboranplus.chinvat.auth.application.usecase;

import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.port.out.AuthClockPort;
import eu.alboranplus.chinvat.auth.application.port.out.AuthSessionPort;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PaginationUtils;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListSessionsPagedUseCase {

  private final AuthSessionPort authSessionPort;
  private final AuthClockPort authClockPort;

  public ListSessionsPagedUseCase(AuthSessionPort authSessionPort, AuthClockPort authClockPort) {
    this.authSessionPort = authSessionPort;
    this.authClockPort = authClockPort;
  }

  @Transactional(readOnly = true)
  public PageResponse<AuthSessionView> execute(
      TokenPrincipal principal, PaginationRequest paginationRequest) {
    Instant now = authClockPort.now();
    Page<AuthSessionView> page =
        authSessionPort.listActiveSessionsByUserIdPaged(
            principal.userId(), now, paginationRequest.toPageable());
    return PaginationUtils.toPageResponse(page);
  }
}
