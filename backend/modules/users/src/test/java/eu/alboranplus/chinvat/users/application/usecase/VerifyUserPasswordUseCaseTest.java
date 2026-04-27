package eu.alboranplus.chinvat.users.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordHasherPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyUserPasswordUseCaseTest {

  @Mock private UsersRepositoryPort usersRepositoryPort;
  @Mock private UsersPasswordPort usersPasswordPort;
  @Mock private UsersPasswordHasherPort usersPasswordHasherPort;

  @InjectMocks private VerifyUserPasswordUseCase sut;

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final String STORED_HASH = "$2a$10$storedHash";

  private final UserAccount activeUser =
      new UserAccount(
          1L, "alice", "Alice Smith", null,
          UserEmail.of("alice@example.com"),
          UserType.INDIVIDUAL, AccessLevel.NORMAL,
          null, null, null, null, "en", NOW, NOW);

  @Test
  void execute_correctPassword_returnsTrue() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("alice@example.com")))
        .willReturn(Optional.of(activeUser));
    given(usersPasswordPort.findHashByUserId(1L)).willReturn(Optional.of(STORED_HASH));
    given(usersPasswordHasherPort.matches("correct-secret", STORED_HASH)).willReturn(true);

    assertThat(sut.execute("alice@example.com", "correct-secret")).isTrue();
  }

  @Test
  void execute_wrongPassword_returnsFalse() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("alice@example.com")))
        .willReturn(Optional.of(activeUser));
    given(usersPasswordPort.findHashByUserId(1L)).willReturn(Optional.of(STORED_HASH));
    given(usersPasswordHasherPort.matches("wrong-secret", STORED_HASH)).willReturn(false);

    assertThat(sut.execute("alice@example.com", "wrong-secret")).isFalse();
  }

  @Test
  void execute_userNotFound_returnsFalse() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("ghost@example.com")))
        .willReturn(Optional.empty());

    assertThat(sut.execute("ghost@example.com", "any-password")).isFalse();
  }

  @Test
  void execute_noPasswordRecord_returnsFalse() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("alice@example.com")))
        .willReturn(Optional.of(activeUser));
    given(usersPasswordPort.findHashByUserId(1L)).willReturn(Optional.empty());

    assertThat(sut.execute("alice@example.com", "any-password")).isFalse();
  }
}

