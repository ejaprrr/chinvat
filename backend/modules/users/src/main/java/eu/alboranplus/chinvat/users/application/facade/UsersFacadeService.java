package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.usecase.ChangePasswordUseCase;
import eu.alboranplus.chinvat.users.application.usecase.CreateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.DeleteUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersPagedUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserByIdUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.UpdateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UsersFacadeService implements UsersFacade {

  private static final String USERS_BY_ID_CACHE = "users.by-id";
  private static final String USERS_ALL_CACHE = "users.all";

  private final CreateUserUseCase createUserUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetAllUsersPagedUseCase getAllUsersPagedUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final GetUserSecurityViewUseCase getUserSecurityViewUseCase;
  private final VerifyUserPasswordUseCase verifyUserPasswordUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final AuditFacade auditFacade;
  private final PermissionCacheFacade permissionCacheFacade;

  public UsersFacadeService(
      CreateUserUseCase createUserUseCase,
      GetUserByIdUseCase getUserByIdUseCase,
      GetAllUsersUseCase getAllUsersUseCase,
      GetAllUsersPagedUseCase getAllUsersPagedUseCase,
      UpdateUserUseCase updateUserUseCase,
      DeleteUserUseCase deleteUserUseCase,
      GetUserSecurityViewUseCase getUserSecurityViewUseCase,
      VerifyUserPasswordUseCase verifyUserPasswordUseCase,
      ChangePasswordUseCase changePasswordUseCase,
      AuditFacade auditFacade,
      PermissionCacheFacade permissionCacheFacade) {
    this.createUserUseCase = createUserUseCase;
    this.getUserByIdUseCase = getUserByIdUseCase;
    this.getAllUsersUseCase = getAllUsersUseCase;
    this.getAllUsersPagedUseCase = getAllUsersPagedUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.getUserSecurityViewUseCase = getUserSecurityViewUseCase;
    this.verifyUserPasswordUseCase = verifyUserPasswordUseCase;
    this.changePasswordUseCase = changePasswordUseCase;
    this.auditFacade = auditFacade;
    this.permissionCacheFacade = permissionCacheFacade;
  }

  @Override
  @CacheEvict(cacheNames = USERS_ALL_CACHE, allEntries = true)
  public UserView createUser(CreateUserCommand command) {
    UserView created = toView(createUserUseCase.execute(command));
    auditFacade.log(
        "USER_CREATED",
        command.email(),
        created.id(),
        AuditDetails.builder()
            .add("email", created.email())
            .add("username", created.username())
            .add("accessLevel", created.accessLevel())
            .build());
    return created;
  }

  @Override
  @Cacheable(cacheNames = USERS_BY_ID_CACHE, key = "#id")
  public UserView getUserById(UUID id) {
    return toView(getUserByIdUseCase.execute(id));
  }

  @Override
  @Cacheable(cacheNames = USERS_ALL_CACHE)
  public List<UserView> getAllUsers() {
    return getAllUsersUseCase.execute().stream().map(this::toView).toList();
  }

  @Override
  public PageResponse<UserView> getAllUsersPaged(PaginationRequest paginationRequest) {
    PageResponse<UserView> pagedResult = getAllUsersPagedUseCase.execute(paginationRequest);
    return pagedResult;
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(cacheNames = USERS_BY_ID_CACHE, key = "#id"),
        @CacheEvict(cacheNames = USERS_ALL_CACHE, allEntries = true)
      })
  public UserView updateUser(UUID id, UpdateUserCommand command, String actor) {
    UserView updated = toView(updateUserUseCase.execute(id, command));
    permissionCacheFacade.evictUserPermissions(id);
    auditFacade.log(
        "USER_UPDATED",
        actor,
        updated.id(),
        AuditDetails.builder()
            .add("email", updated.email())
            .add("username", updated.username())
            .add("accessLevel", updated.accessLevel())
            .build());
    return updated;
  }

  @Override
  @Caching(
      evict = {
        @CacheEvict(cacheNames = USERS_BY_ID_CACHE, key = "#id"),
        @CacheEvict(cacheNames = USERS_ALL_CACHE, allEntries = true)
      })
  public void deleteUser(UUID id, String actor) {
    deleteUserUseCase.execute(id);
    permissionCacheFacade.evictUserPermissions(id);
    auditFacade.log(
        "USER_DELETED", actor, id, AuditDetails.builder().add("userId", id).build());
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewByEmail(String email) {
    return getUserSecurityViewUseCase.execute(email);
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewById(UUID id) {
    return getUserSecurityViewUseCase.executeById(id);
  }

  @Override
  public Optional<UserSecurityView> findSecurityViewByCertificateThumbprint(
      String thumbprintSha256, Instant now) {
    return getUserSecurityViewUseCase.executeByCertificateThumbprint(thumbprintSha256, now);
  }

  @Override
  public boolean verifyPassword(String email, String rawPassword) {
    return verifyUserPasswordUseCase.execute(email, rawPassword);
  }

  @Override
  public void changePassword(UUID userId, String rawPassword) {
    changePasswordUseCase.execute(new ChangePasswordCommand(userId, rawPassword));
  }

  private UserView toView(UserAccount user) {
    return new UserView(
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
        user.defaultLanguage());
  }
}

