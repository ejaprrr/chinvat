package eu.alboranplus.chinvat.rbac.application.usecase;

import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PaginationUtils;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ListPermissionsPagedUseCase {

  private final RbacRepositoryPort rbacRepositoryPort;

  public ListPermissionsPagedUseCase(RbacRepositoryPort rbacRepositoryPort) {
    this.rbacRepositoryPort = rbacRepositoryPort;
  }

  public PageResponse<PermissionView> execute(PaginationRequest paginationRequest) {
    Page<PermissionView> page =
        rbacRepositoryPort
            .findAllPermissions(paginationRequest.toPageable())
            .map(p -> new PermissionView(p.permissionCode(), p.description()));
    return PaginationUtils.toPageResponse(page);
  }
}
