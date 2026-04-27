package eu.alboranplus.chinvat.users.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserEmailTest {

  @Test
  void of_validEmail_normalizesToLowerCase() {
    UserEmail email = UserEmail.of("User@Example.COM");
    assertThat(email.value()).isEqualTo("user@example.com");
  }

  @Test
  void of_alreadyLowercase_unchanged() {
    UserEmail email = UserEmail.of("alice@example.com");
    assertThat(email.value()).isEqualTo("alice@example.com");
  }

  @Test
  void of_trailingWhitespace_trimmed() {
    UserEmail email = UserEmail.of("  alice@example.com  ");
    assertThat(email.value()).isEqualTo("alice@example.com");
  }

  @ParameterizedTest
  @ValueSource(strings = {"notanemail", "@nodomain", "missing@", "two@@at.com", "a b@c.com"})
  void of_invalidFormat_throwsIllegalArgument(String invalid) {
    assertThatThrownBy(() -> UserEmail.of(invalid))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("invalid");
  }

  @Test
  void of_null_throwsIllegalArgument() {
    assertThatThrownBy(() -> UserEmail.of(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void of_blank_throwsIllegalArgument() {
    assertThatThrownBy(() -> UserEmail.of("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void equality_sameValue_equal() {
    assertThat(UserEmail.of("alice@example.com")).isEqualTo(UserEmail.of("ALICE@EXAMPLE.COM"));
  }
}
