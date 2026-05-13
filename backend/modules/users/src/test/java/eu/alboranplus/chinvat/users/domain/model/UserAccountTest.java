package eu.alboranplus.chinvat.users.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserAccountTest {

  private static final UserEmail EMAIL = UserEmail.of("alice@example.com");
  private static final String USERNAME = "alice";
  private static final String FULL_NAME = "Alice Smith";
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void newUser_createsAccountWithNullId() {
    UserAccount account =
        UserAccount.newUser(
            USERNAME, FULL_NAME, null, EMAIL, UserType.INDIVIDUAL, AccessLevel.NORMAL,
            null, null, null, null, "en", NOW);

    assertThat(account.id()).isNull();
    assertThat(account.username()).isEqualTo(USERNAME);
    assertThat(account.fullName()).isEqualTo(FULL_NAME);
    assertThat(account.email()).isEqualTo(EMAIL);
    assertThat(account.userType()).isEqualTo(UserType.INDIVIDUAL);
    assertThat(account.accessLevel()).isEqualTo(AccessLevel.NORMAL);
    assertThat(account.defaultLanguage()).isEqualTo("en");
    assertThat(account.createdAt()).isEqualTo(NOW);
    assertThat(account.updatedAt()).isEqualTo(NOW);
  }

  @Test
  void newUser_tripsFullName() {
    UserAccount account =
        UserAccount.newUser(
            USERNAME, "  Alice Smith  ", null, EMAIL, UserType.INDIVIDUAL, AccessLevel.NORMAL,
            null, null, null, null, "en", NOW);
    assertThat(account.fullName()).isEqualTo("Alice Smith");
  }

  @Test
  void constructor_blankFullName_throwsIllegalArgument() {
    assertThatThrownBy(
            () -> new UserAccount(null, USERNAME, "  ", null, EMAIL,
        UserType.INDIVIDUAL, AccessLevel.NORMAL, null, null, null, null, "en", NOW, NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Full name");
  }

  @Test
  void constructor_blankUsername_throwsIllegalArgument() {
    assertThatThrownBy(
            () -> new UserAccount(null, "  ", FULL_NAME, null, EMAIL,
        UserType.INDIVIDUAL, AccessLevel.NORMAL, null, null, null, null, "en", NOW, NOW, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Username");
  }

  @Test
  void withUpdatedFields_returnsNewRecordWithUpdatedValues() {
    UserAccount original =
        UserAccount.newUser(
            USERNAME, FULL_NAME, null, EMAIL, UserType.INDIVIDUAL, AccessLevel.NORMAL,
            null, null, null, null, "en", NOW);
    Instant later = Instant.parse("2026-06-01T00:00:00Z");

    UserAccount updated = original.withUpdatedFields(
        "alice_new", "Alice New", "+34 600 000 000", UserType.INDIVIDUAL, AccessLevel.GOLD,
        "Street 1", "29001", "Malaga", "Spain", "es", later);

    assertThat(updated.id()).isEqualTo(original.id());
    assertThat(updated.email()).isEqualTo(original.email());
    assertThat(updated.username()).isEqualTo("alice_new");
    assertThat(updated.fullName()).isEqualTo("Alice New");
    assertThat(updated.accessLevel()).isEqualTo(AccessLevel.GOLD);
    assertThat(updated.updatedAt()).isEqualTo(later);
    assertThat(updated.createdAt()).isEqualTo(NOW);
  }
}

