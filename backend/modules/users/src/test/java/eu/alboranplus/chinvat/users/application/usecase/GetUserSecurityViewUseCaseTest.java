package eu.alboranplus.chinvat.users.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.application.port.out.UsersRoleRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserSecurityViewUseCaseTest {

  @Mock private UsersRepositoryPort usersRepositoryPort;
  @Mock private UsersRoleRepositoryPort usersRoleRepositoryPort;

  @InjectMocks private GetUserSecurityViewUseCase sut;

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  private static final UUID TEST_UUID = UUID.fromString("00000000-0000-0000-0000-00000000002a");

  private final UserAccount existingUser =
      new UserAccount(
          TEST_UUID, "alice", "Alice Smith", null,
          UserEmail.of("alice@example.com"),
          UserType.INDIVIDUAL, AccessLevel.GOLD,
          null, null, null, null, "en", NOW, NOW);

  @Test
  void execute_byEmail_found_returnsMappedView() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("alice@example.com")))
        .willReturn(Optional.of(existingUser));
    given(usersRoleRepositoryPort.findRoleNamesByUserId(TEST_UUID)).willReturn(Set.of("USER"));

    Optional<UserSecurityView> result = sut.execute("alice@example.com");

    assertThat(result).isPresent();
    UserSecurityView view = result.get();
    assertThat(view.id()).isEqualTo(TEST_UUID);
    assertThat(view.email()).isEqualTo("alice@example.com");
    assertThat(view.displayName()).isEqualTo("Alice Smith");
    assertThat(view.roles()).containsExactlyInAnyOrder("GOLD", "USER");
    assertThat(view.active()).isTrue();
  }

  @Test
  void execute_byEmail_notFound_returnsEmpty() {
    given(usersRepositoryPort.findByEmail(UserEmail.of("ghost@example.com")))
        .willReturn(Optional.empty());

    assertThat(sut.execute("ghost@example.com")).isEmpty();
  }

  @Test
  void executeById_found_returnsMappedView() {
    given(usersRepositoryPort.findById(TEST_UUID)).willReturn(Optional.of(existingUser));
    given(usersRoleRepositoryPort.findRoleNamesByUserId(TEST_UUID)).willReturn(Set.of());

    Optional<UserSecurityView> result = sut.executeById(TEST_UUID);

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(TEST_UUID);
  }

  @Test
  void executeById_notFound_returnsEmpty() {
    given(usersRepositoryPort.findById(UUID.fromString("00000000-0000-0000-0000-0000000003e7"))).willReturn(Optional.empty());

    assertThat(sut.executeById(UUID.fromString("00000000-0000-0000-0000-0000000003e7"))).isEmpty();
  }
}

