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
import eu.alboranplus.chinvat.users.application.usecase.GetAllUsersUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserByIdUseCase;
import eu.alboranplus.chinvat.users.application.usecase.GetUserSecurityViewUseCase;
import eu.alboranplus.chinvat.users.application.usecase.UpdateUserUseCase;
import eu.alboranplus.chinvat.users.application.usecase.VerifyUserPasswordUseCase;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Map;
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
  @Mock private UpdateUserUseCase updateUserUseCase;
  @Mock private DeleteUserUseCase deleteUserUseCase;
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
            updateUserUseCase,
            deleteUserUseCase,
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
    given(createUserUseCase.execute(command)).willReturn(user(11L, "alice", "alice@example.com"));

    sut.createUser(command);

    then(auditFacade)
        .should()
        .log(
            eq("USER_CREATED"),
            eq("alice@example.com"),
            eq(11L),
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
    given(updateUserUseCase.execute(11L, command)).willReturn(user(11L, "alice", "alice@example.com"));

    sut.updateUser(11L, command, "admin@example.com");

    then(permissionCacheFacade).should().evictUserPermissions(11L);
    then(auditFacade)
        .should()
        .log(
            eq("USER_UPDATED"),
            eq("admin@example.com"),
            eq(11L),
            eq(
                Map.of(
                    "email",
                    "alice@example.com",
                    "username",
                    "alice",
                    "accessLevel",
                    AccessLevel.NORMAL)));
  }

  private static UserAccount user(Long id, String username, String email) {
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
        Instant.parse("2026-01-01T00:00:00Z"));
  }
}