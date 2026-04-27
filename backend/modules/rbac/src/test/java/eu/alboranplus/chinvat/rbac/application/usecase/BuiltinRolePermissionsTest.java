package eu.alboranplus.chinvat.rbac.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuiltinRolePermissionsTest {

  @Test
  void user_hasProfileRead() {
    assertThat(BuiltinRolePermissions.permissionsFor("USER")).containsExactly("PROFILE:READ");
  }

  @Test
  void admin_hasExpectedPermissions() {
    assertThat(BuiltinRolePermissions.permissionsFor("ADMIN"))
        .containsExactlyInAnyOrder("PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE");
  }

  @Test
  void superadmin_hasAllPermissions() {
    assertThat(BuiltinRolePermissions.permissionsFor("SUPERADMIN"))
        .containsExactlyInAnyOrder(
            "PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE", "RBAC:MANAGE", "AUTH:MANAGE");
  }

  @Test
  void permissionsFor_isCaseInsensitive_lowercaseInput() {
    assertThat(BuiltinRolePermissions.permissionsFor("user"))
        .containsExactly("PROFILE:READ");
  }

  @ParameterizedTest
  @ValueSource(strings = {"UNKNOWN", "GUEST", "", "MANAGER"})
  void permissionsFor_unknownRole_returnsEmptySet(String role) {
    assertThat(BuiltinRolePermissions.permissionsFor(role)).isEmpty();
  }

  @Test
  void permissionsAreImmutable() {
    java.util.Set<String> perms = BuiltinRolePermissions.permissionsFor("USER");
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> perms.add("HACK"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
