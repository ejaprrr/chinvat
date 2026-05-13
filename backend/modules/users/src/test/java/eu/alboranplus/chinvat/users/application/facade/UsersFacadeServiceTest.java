package eu.alboranplus.chinvat.users.application.facade;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.command.UpdateUserCommand;
import eu.alboranplus.chinvat.users.application.usecase.ChangePasswordUseCase;
import eu.alboranplus.chinvat.users.application.usecase.CreateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.DeleteUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersPagedUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserByIdUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.PermanentDeleteUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.RestoreUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.UpdateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsersFacadeServiceTest {

  @Mock private CreateUserUseCase createUserUseCase;
  @Mock private GetUserByIdUseCase getUserByIdUseCase;
  @Mock private GetAllUsersUseCase getAllUsersUseCase;
  @Mock private GetAllUsersPagedUseCase getAllUsersPagedUseCase;
  @Mock private UpdateUserUseCase updateUserUseCase;
  @Mock private DeleteUserUseCase deleteUserUseCase;
  @Mock private RestoreUserUseCase restoreUserUseCase;
  @Mock private PermanentDeleteUserUseCase permanentDeleteUserUseCase;
  @Mock private GetUserSecurityViewUseCase getUserSecurityViewUseCase;
  @Mock private VerifyUserPasswordUseCase verifyUserPasswordUseCase;
  @Mock private ChangePasswordUseCase changePasswordUseCase;
  @Mock private AuditFacade auditFacade;
    @Mock private PermissionCacheFacade permissionCacheFacade;

  private UsersFacadeService sut;

  @BeforeEach
  void setUp() {
    sut =
        new UsersFacadeService(
            createUserUseCase,
            getUserByIdUseCase,
            getAllUsersUseCase,
            getAllUsersPagedUseCase,
            updateUserUseCase,
            deleteUserUseCase,
            restoreUserUseCase,
            permanentDeleteUserUseCase,
            getUserSecurityViewUseCase,
            verifyUserPasswordUseCase,
            changePasswordUseCase,
            auditFacade,
            permissionCacheFacade);
  }

  @Test
  void createUser_logsSharedAuditEvent() {
    CreateUserCommand command =
        new CreateUserCommand(
            "alice",
            "Alice",
            null,
            "alice@example.com",
            "Secret123!",
            UserType.INDIVIDUAL,
            AccessLevel.NORMAL,
            null,
            null,
            null,
            null,
            "en");
    given(createUserUseCase.execute(command)).willReturn(user(UUID.fromString("00000000-0000-0000-0000-00000000000b"), "alice", "alice@example.com"));

    sut.createUser(command);

    then(auditFacade)
        .should()
        .log(
            eq("USER_CREATED"),
            eq("alice@example.com"),
            eq(UUID.fromString("00000000-0000-0000-0000-00000000000b")),
            eq(
                Map.of(
                    "email",
                    "alice@example.com",
                    "username",
                    "alice",
                    "accessLevel",
                    AccessLevel.NORMAL)));
  }

  @Test
  void updateUser_logsSharedAuditEvent() {
    UpdateUserCommand command =
        new UpdateUserCommand(
            "alice",
            "Alice Smith",
            null,
            UserType.INDIVIDUAL,
            AccessLevel.ADMIN,
            null,
            null,
            null,
            null,
            "en");
    given(updateUserUseCase.execute(UUID.fromString("00000000-0000-0000-0000-00000000000b"), command)).willReturn(user(UUID.fromString("00000000-0000-0000-0000-00000000000b"), "alice", "alice@example.com"));

    sut.updateUser(UUID.fromString("00000000-0000-0000-0000-00000000000b"), command, "admin@example.com");

    then(permissionCacheFacade).should().evictUserPermissions(UUID.fromString("00000000-0000-0000-0000-00000000000b"));
    then(auditFacade)
        .should()
        .log(
            eq("USER_UPDATED"),
            eq("admin@example.com"),
            eq(UUID.fromString("00000000-0000-0000-0000-00000000000b")),
            eq(
                Map.of(
                    "email",
                    "alice@example.com",
                    "username",
                    "alice",
                    "accessLevel",
                    AccessLevel.NORMAL)));
  }

  private static UserAccount user(UUID id, String username, String email) {
    return new UserAccount(
        id,
        username,
        "Alice",
        null,
        new UserEmail(email),
        UserType.INDIVIDUAL,
        AccessLevel.NORMAL,
        null,
        null,
        null,
        null,
        "en",
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-01-01T00:00:00Z"),
        null);
  }
}