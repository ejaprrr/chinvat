package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsersFacade {
  UserView createUser(CreateUserCommand command);

  UserView getUserById(Long id);

  List<UserView> getAllUsers();

  PageResponse<UserView> getAllUsersPaged(PaginationRequest paginationRequest);

  UserView updateUser(Long id, UpdateUserCommand command, String actor);

  void deleteUser(Long id, String actor);

  Optional<UserSecurityView> findSecurityViewByEmail(String email);

  Optional<UserSecurityView> findSecurityViewById(Long id);

  Optional<UserSecurityView> findSecurityViewByCertificateThumbprint(String thumbprintSha256, Instant now);

  boolean verifyPassword(String email, String rawPassword);

  void changePassword(Long userId, String rawPassword);
}

