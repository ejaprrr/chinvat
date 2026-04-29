package eu.alboranplus.chinvat.rbac.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity.RoleJpaEntity;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.jpa.RoleJpaRepository;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.mapper.RoleJpaMapper;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import eu.alboranplus.chinvat.rbac.RbacTestApplication;

@SpringBootTest(classes = RbacTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Import(RoleJpaMapper.class)
class RbacRepositoryAdapterIT {

  @Autowired private RoleJpaRepository roleJpaRepository;
  @Autowired private RoleJpaMapper roleJpaMapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  private RbacRepositoryAdapter sut;

  @BeforeEach
  void setUp() {
    sut = new RbacRepositoryAdapter(roleJpaRepository, roleJpaMapper, jdbcTemplate);
  }

  @Test
  void findByRoleName_existing_returnsRole() {
    persistRole("CUSTOM_ROLE", "CUSTOM:READ,CUSTOM:WRITE");

    Optional<RoleDefinition> result = sut.findByRoleName("CUSTOM_ROLE");

    assertThat(result).isPresent();
    assertThat(result.get().roleName()).isEqualTo("CUSTOM_ROLE");
    assertThat(result.get().permissions()).containsExactlyInAnyOrder("CUSTOM:READ", "CUSTOM:WRITE");
  }

  @Test
  void findByRoleName_caseInsensitive_returnsRole() {
    persistRole("SPECIAL", "SPECIAL:PERM");

    Optional<RoleDefinition> result = sut.findByRoleName("special");

    assertThat(result).isPresent();
  }

  @Test
  void findByRoleName_notFound_returnsEmpty() {
    assertThat(sut.findByRoleName("NONEXISTENT")).isEmpty();
  }

  @Test
  void findByRoleNames_multipleExist_returnsAll() {
    persistRole("ROLE_A", "A:PERM");
    persistRole("ROLE_B", "B:PERM");
    persistRole("ROLE_C", "C:PERM");

    Set<RoleDefinition> result = sut.findByRoleNames(Set.of("ROLE_A", "ROLE_B"));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(RoleDefinition::roleName)
        .containsExactlyInAnyOrder("ROLE_A", "ROLE_B");
  }

  @Test
  void findByRoleNames_noneMatch_returnsEmpty() {
    assertThat(sut.findByRoleNames(Set.of("GHOST_1", "GHOST_2"))).isEmpty();
  }

  @Test
  void findByRoleNames_emptyInput_returnsEmpty() {
    assertThat(sut.findByRoleNames(Set.of())).isEmpty();
  }

  // --- helpers ---

  private void persistRole(String roleName, String permissionsCsv) {
    RoleJpaEntity entity = new RoleJpaEntity();
    entity.setRoleName(roleName);
    entity.setPermissionsCsv(permissionsCsv);
    roleJpaRepository.save(entity);
  }
}
