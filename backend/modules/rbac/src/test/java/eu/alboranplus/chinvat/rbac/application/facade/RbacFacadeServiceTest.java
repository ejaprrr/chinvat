package eu.alboranplus.chinvat.rbac.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.common.cache.PermissionCacheFacade;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.rbac.application.usecase.AssignRoleToUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.CreatePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.DeletePermissionUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetRoleUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.GetUserRolesUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ListPermissionsPagedUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ListPermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.RemoveRoleFromUserUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.ResolvePermissionsUseCase;
import eu.alboranplus.chinvat.rbac.application.usecase.UpdatePermissionUseCase;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RbacFacadeServiceTest {

  @Mock private GetRoleUseCase getRoleUseCase;
  @Mock private ResolvePermissionsUseCase resolvePermissionsUseCase;
  @Mock private ListPermissionsUseCase listPermissionsUseCase;
  @Mock private ListPermissionsPagedUseCase listPermissionsPagedUseCase;
  @Mock private CreatePermissionUseCase createPermissionUseCase;
  @Mock private UpdatePermissionUseCase updatePermissionUseCase;
  @Mock private DeletePermissionUseCase deletePermissionUseCase;
  @Mock private GetUserRolesUseCase getUserRolesUseCase;
  @Mock private AssignRoleToUserUseCase assignRoleToUserUseCase;
  @Mock private RemoveRoleFromUserUseCase removeRoleFromUserUseCase;
  @Mock private AuditFacade auditFacade;
  @Mock private PermissionCacheFacade permissionCacheFacade;

  private RbacFacadeService sut;

  @BeforeEach
  void setUp() {
    sut =
        new RbacFacadeService(
            getRoleUseCase,
            resolvePermissionsUseCase,
            listPermissionsUseCase,
            listPermissionsPagedUseCase,
            createPermissionUseCase,
            updatePermissionUseCase,
            deletePermissionUseCase,
            getUserRolesUseCase,
            assignRoleToUserUseCase,
            removeRoleFromUserUseCase,
            auditFacade,
            permissionCacheFacade);
  }

  @Test
  void createPermission_logsAuditEventAfterSuccessfulCreation() {
    given(createPermissionUseCase.execute("RBAC:EXPORT", "Export RBAC data"))
        .willReturn(new PermissionView("RBAC:EXPORT", "Export RBAC data"));

    PermissionView result =
        sut.createPermission("RBAC:EXPORT", "Export RBAC data", "rbac-admin@example.com");

    assertThat(result.code()).isEqualTo("RBAC:EXPORT");
    then(permissionCacheFacade).should().evictAllUserPermissions();
    then(auditFacade)
        .should()
        .log(
            eq("RBAC_PERMISSION_CREATED"),
            eq("rbac-admin@example.com"),
            eq(null),
            eq(Map.of("permissionCode", "RBAC:EXPORT", "description", "Export RBAC data")));
  }

  @Test
  void deletePermission_logsAuditEventAfterSuccessfulDeletion() {
    sut.deletePermission("RBAC:EXPORT", "rbac-admin@example.com");

    then(deletePermissionUseCase).should().execute("RBAC:EXPORT");
    then(permissionCacheFacade).should().evictAllUserPermissions();
    then(auditFacade)
        .should()
        .log(
            eq("RBAC_PERMISSION_DELETED"),
            eq("rbac-admin@example.com"),
            eq(null),
            eq(Map.of("permissionCode", "RBAC:EXPORT")));
  }

  private static final UUID TEST_USER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000063");

  @Test
  void assignRoleToUser_logsAssignedByAsActor() {
    sut.assignRoleToUser(TEST_USER_UUID, "ADMIN", "rbac-admin@example.com");

    then(assignRoleToUserUseCase).should().execute(TEST_USER_UUID, "ADMIN", "rbac-admin@example.com");
    then(permissionCacheFacade).should().evictUserPermissions(TEST_USER_UUID);
    then(auditFacade)
        .should()
        .log(
            eq("RBAC_ROLE_ASSIGNED_TO_USER"),
            eq("rbac-admin@example.com"),
            eq(null),
            eq(
                Map.of(
                    "userId",
                    TEST_USER_UUID,
                    "roleName",
                    "ADMIN",
                    "assignedBy",
                    "rbac-admin@example.com")));
  }

  @Test
  void removeRoleFromUser_logsActor() {
    sut.removeRoleFromUser(TEST_USER_UUID, "ADMIN", "rbac-admin@example.com");

    then(removeRoleFromUserUseCase).should().execute(TEST_USER_UUID, "ADMIN");
    then(permissionCacheFacade).should().evictUserPermissions(TEST_USER_UUID);
    then(auditFacade)
        .should()
        .log(
            eq("RBAC_ROLE_REMOVED_FROM_USER"),
            eq("rbac-admin@example.com"),
            eq(null),
            eq(Map.of("userId", TEST_USER_UUID, "roleName", "ADMIN")));
  }

  @Test
  void updatePermission_omitsNullDescriptionFromAuditDetails() {
    given(updatePermissionUseCase.execute("RBAC:EXPORT", null))
        .willReturn(new PermissionView("RBAC:EXPORT", null));

    sut.updatePermission("RBAC:EXPORT", null, "rbac-admin@example.com");

    ArgumentCaptor<Map<String, Object>> detailsCaptor = ArgumentCaptor.forClass(Map.class);
    then(auditFacade)
        .should()
        .log(
            eq("RBAC_PERMISSION_UPDATED"),
            eq("rbac-admin@example.com"),
            eq(null),
            detailsCaptor.capture());
    assertThat(detailsCaptor.getValue()).containsEntry("permissionCode", "RBAC:EXPORT");
    assertThat(detailsCaptor.getValue()).doesNotContainKey("description");
  }
}