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
import java.util.UUID;

public interface UsersFacade {
  UserView createUser(CreateUserCommand command);

  UserView getUserById(UUID id);

  List<UserView> getAllUsers();

  PageResponse<UserView> getAllUsersPaged(PaginationRequest paginationRequest);

  UserView updateUser(UUID id, UpdateUserCommand command, String actor);

  void deleteUser(UUID id, String actor);

  /**
   * Restore a soft-deleted user account.
   * @param id user ID to restore
   * @param actor who is performing the restoration (for audit)
   * @return restored user view
   */
  UserView restoreUser(UUID id, String actor);

  /**
   * Permanently delete a user account (hard delete - irreversible).
   * Admin-only operation after compliance review.
   * @param id user ID to permanently delete
   * @param actor who is performing the deletion (for audit)
   */
  void permanentlyDeleteUser(UUID id, String actor);

  Optional<UserSecurityView> findSecurityViewByEmail(String email);

  Optional<UserSecurityView> findSecurityViewById(UUID id);

  Optional<UserSecurityView> findSecurityViewByCertificateThumbprint(String thumbprintSha256, Instant now);

  boolean verifyPassword(String email, String rawPassword);

  void changePassword(UUID userId, String rawPassword);
}

