package eu.alboranplus.chinvat.users.application.usecase;

import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationUtils;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Get all users with pagination support.
 *
 * Returns a PageResponse containing users list and pagination metadata.
 */
@Service
public class GetAllUsersPagedUseCase {

  private final UsersRepositoryPort usersRepositoryPort;

  public GetAllUsersPagedUseCase(UsersRepositoryPort usersRepositoryPort) {
    this.usersRepositoryPort = usersRepositoryPort;
  }

  @Transactional(readOnly = true)
  public PageResponse<UserView> execute(PaginationRequest paginationRequest) {
    Page<UserAccount> page = usersRepositoryPort.findAll(paginationRequest.toPageable());
    Page<UserView> mappedPage =
        page.map(
            user ->
                new UserView(
                    user.id(),
                    user.username(),
                    user.fullName(),
                    user.phoneNumber(),
                    user.email().value(),
                    user.userType(),
                    user.accessLevel(),
                    user.addressLine(),
                    user.postalCode(),
                    user.city(),
                    user.country(),
                    user.defaultLanguage()));

    return PaginationUtils.toPageResponse(mappedPage);
  }
}
