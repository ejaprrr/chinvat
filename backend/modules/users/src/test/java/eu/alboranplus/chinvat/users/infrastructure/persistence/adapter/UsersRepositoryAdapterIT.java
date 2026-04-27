package eu.alboranplus.chinvat.users.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.users.UsersTestApplication;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import eu.alboranplus.chinvat.users.infrastructure.persistence.jpa.UserAccountJpaRepository;
import eu.alboranplus.chinvat.users.infrastructure.persistence.mapper.UserAccountJpaMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    classes = UsersTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Import(UserAccountJpaMapper.class)
class UsersRepositoryAdapterIT {

  @Autowired private UserAccountJpaRepository jpaRepository;
  @Autowired private UserAccountJpaMapper mapper;

  private UsersRepositoryAdapter sut;

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    sut = new UsersRepositoryAdapter(jpaRepository, mapper);
  }

  @Test
  void save_persistsAndReturnsWithId() {
    UserAccount unsaved =
        UserAccount.newUser(
            "alice", "Alice Smith", null,
            UserEmail.of("alice@example.com"),
            UserType.INDIVIDUAL, AccessLevel.NORMAL,
            null, null, null, null, "en", NOW);

    UserAccount saved = sut.save(unsaved);

    assertThat(saved.id()).isNotNull();
    assertThat(saved.email().value()).isEqualTo("alice@example.com");
    assertThat(saved.username()).isEqualTo("alice");
    assertThat(saved.accessLevel()).isEqualTo(AccessLevel.NORMAL);
  }

  @Test
  void existsByEmail_existingEmail_returnsTrue() {
    persistUser("alice2", "exists@example.com");

    assertThat(sut.existsByEmail(UserEmail.of("exists@example.com"))).isTrue();
  }

  @Test
  void existsByEmail_caseInsensitive_returnsTrue() {
    persistUser("lower", "lower@example.com");

    assertThat(sut.existsByEmail(UserEmail.of("LOWER@EXAMPLE.COM"))).isTrue();
  }

  @Test
  void existsByEmail_unknown_returnsFalse() {
    assertThat(sut.existsByEmail(UserEmail.of("ghost@example.com"))).isFalse();
  }

  @Test
  void existsByUsername_existingUsername_returnsTrue() {
    persistUser("takenuser", "taken@example.com");

    assertThat(sut.existsByUsername("takenuser")).isTrue();
  }

  @Test
  void existsByUsername_unknown_returnsFalse() {
    assertThat(sut.existsByUsername("noone")).isFalse();
  }

  @Test
  void findByEmail_existingEmail_returnsUser() {
    persistUser("findme", "findme@example.com");

    Optional<UserAccount> result = sut.findByEmail(UserEmail.of("findme@example.com"));

    assertThat(result).isPresent();
    assertThat(result.get().email().value()).isEqualTo("findme@example.com");
  }

  @Test
  void findByEmail_notFound_returnsEmpty() {
    assertThat(sut.findByEmail(UserEmail.of("ghost@example.com"))).isEmpty();
  }

  @Test
  void findById_existingId_returnsUser() {
    UserAccount saved =
        sut.save(
            UserAccount.newUser(
                "byid", "ById User", null,
                UserEmail.of("byid@example.com"),
                UserType.INDIVIDUAL, AccessLevel.NORMAL,
                null, null, null, null, "en", NOW));

    Optional<UserAccount> result = sut.findById(saved.id());

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(saved.id());
  }

  @Test
  void findById_notFound_returnsEmpty() {
    assertThat(sut.findById(999_999L)).isEmpty();
  }

  @Test
  void findAll_returnsPersistedUsers() {
    persistUser("first", "first@example.com");
    persistUser("second", "second@example.com");

    List<UserAccount> all = sut.findAll();

    assertThat(all).extracting(user -> user.email().value())
        .contains("first@example.com", "second@example.com");
  }

  @Test
  void deleteById_removesUser() {
    UserAccount saved =
        sut.save(
            UserAccount.newUser(
                "todelete", "To Delete", null,
                UserEmail.of("delete@example.com"),
                UserType.INDIVIDUAL, AccessLevel.NORMAL,
                null, null, null, null, "en", NOW));

    sut.deleteById(saved.id());

    assertThat(sut.findById(saved.id())).isEmpty();
  }

  // --- helpers ---

  private void persistUser(String username, String email) {
    UserAccountJpaEntity entity = new UserAccountJpaEntity();
    entity.setUsername(username);
    entity.setFullName("Test User");
    entity.setEmail(email);
    entity.setUserType(UserType.INDIVIDUAL);
    entity.setAccessLevel(AccessLevel.NORMAL);
    entity.setDefaultLanguage("en");
    entity.setCreatedAt(NOW);
    entity.setUpdatedAt(NOW);
    jpaRepository.save(entity);
  }
}

