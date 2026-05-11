package eu.alboranplus.chinvat.users.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.port.out.UsersClockPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private UsersRepositoryPort usersRepositoryPort;
  @Mock private UsersPasswordPort usersPasswordPort;
  @Mock private UsersPasswordHasherPort usersPasswordHasherPort;
  @Mock private UsersClockPort usersClockPort;

  @InjectMocks private CreateUserUseCase sut;

  private CreateUserCommand buildCommand(String username, String email) {
    return new CreateUserCommand(
        username, "Alice Smith", null, email, "SecretPassword1!",
        UserType.INDIVIDUAL, AccessLevel.NORMAL, null, null, null, null, "en");
  }

  private UserAccount buildSaved(UUID id, String username, String email) {
    return UserAccount.newUser(
        username, "Alice Smith", null, UserEmail.of(email), UserType.INDIVIDUAL,
        AccessLevel.NORMAL, null, null, null, null, "en", FIXED_INSTANT);
  }

  @Test
  void execute_happyPath_savesUserAndPassword() {
    CreateUserCommand cmd = buildCommand("alice", "alice@example.com");
    given(usersRepositoryPort.existsByEmail(UserEmail.of("alice@example.com"))).willReturn(false);
    given(usersRepositoryPort.existsByUsername("alice")).willReturn(false);
    given(usersPasswordHasherPort.hash("SecretPassword1!")).willReturn("$bcrypt_hash$");
    given(usersClockPort.now()).willReturn(FIXED_INSTANT);
    UserAccount expected = buildSaved(UUID.fromString("00000000-0000-0000-0000-000000000001"), "alice", "alice@example.com");
    given(usersRepositoryPort.save(any())).willReturn(expected);

    UserAccount result = sut.execute(cmd);

    ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
    verify(usersRepositoryPort).save(captor.capture());
    UserAccount saved = captor.getValue();
    assertThat(saved.email().value()).isEqualTo("alice@example.com");
    assertThat(saved.username()).isEqualTo("alice");
    assertThat(result).isSameAs(expected);
    verify(usersPasswordPort).save(eq(expected.id()), eq("$bcrypt_hash$"), eq("bcrypt"));
  }

  @Test
  void execute_duplicateEmail_throwsUserAlreadyExistsException() {
    CreateUserCommand cmd = buildCommand("alice", "existing@example.com");
    given(usersRepositoryPort.existsByEmail(UserEmail.of("existing@example.com"))).willReturn(true);

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("existing@example.com");
  }

  @Test
  void execute_duplicateUsername_throwsUserAlreadyExistsException() {
    CreateUserCommand cmd = buildCommand("taken", "new@example.com");
    given(usersRepositoryPort.existsByEmail(UserEmail.of("new@example.com"))).willReturn(false);
    given(usersRepositoryPort.existsByUsername("taken")).willReturn(true);

    assertThatThrownBy(() -> sut.execute(cmd))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("taken");
  }
}
