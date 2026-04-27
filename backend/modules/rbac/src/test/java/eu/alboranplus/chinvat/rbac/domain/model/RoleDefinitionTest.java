package eu.alboranplus.chinvat.rbac.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleDefinitionTest {

  @Test
  void constructor_normalizesRoleNameToUpperCase() {
    RoleDefinition role = new RoleDefinition("user", Set.of("PROFILE:READ"));
    assertThat(role.roleName()).isEqualTo("USER");
  }

  @Test
  void constructor_tripsRoleName() {
    RoleDefinition role = new RoleDefinition("  admin  ", Set.of());
    assertThat(role.roleName()).isEqualTo("ADMIN");
  }

  @Test
  void constructor_nullRoleName_throwsIllegalArgument() {
    assertThatThrownBy(() -> new RoleDefinition(null, Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Role name");
  }

  @Test
  void constructor_blankRoleName_throwsIllegalArgument() {
    assertThatThrownBy(() -> new RoleDefinition("  ", Set.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Role name");
  }

  @Test
  void constructor_permissionsAreCopied_originalMutationDoesNotAffect() {
    Set<String> mutable = new HashSet<>();
    mutable.add("PROFILE:READ");
    RoleDefinition role = new RoleDefinition("USER", mutable);
    mutable.add("PROFILE:WRITE");
    assertThat(role.permissions()).containsExactly("PROFILE:READ");
  }

  @Test
  void permissions_areImmutable() {
    RoleDefinition role = new RoleDefinition("USER", Set.of("PROFILE:READ"));
    assertThatThrownBy(() -> role.permissions().add("EXTRA"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
