package eu.alboranplus.chinvat.users.application.facade;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.command.ChangePasswordCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.usecase.ChangePasswordUseCase;
import eu.alboranplus.chinvat.users.application.usecase.CreateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.DeleteUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserByIdUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.UpdateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UsersFacadeService implements UsersFacade {

  private final CreateUserUseCase createUserUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetAllUsersUseCase getAllUsersUseCase;
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
    this.updateUserUseCase = updateUserUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.getUserSecurityViewUseCase = getUserSecurityViewUseCase;
    this.verifyUserPasswordUseCase = verifyUserPasswordUseCase;
    this.changePasswordUseCase = changePasswordUseCase;
    this.auditFacade = auditFacade;
    this.permissionCacheFacade = permissionCacheFacade;
  }

  @Override
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
  public UserView getUserById(Long id) {
    return toView(getUserByIdUseCase.execute(id));
  }

  @Override
  public List<UserView> getAllUsers() {
    return getAllUsersUseCase.execute().stream().map(this::toView).toList();
  }

  @Override
  public UserView updateUser(Long id, UpdateUserCommand command, String actor) {
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
  public void deleteUser(Long id, String actor) {
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
  public Optional<UserSecurityView> findSecurityViewById(Long id) {
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
  public void changePassword(Long userId, String rawPassword) {
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

