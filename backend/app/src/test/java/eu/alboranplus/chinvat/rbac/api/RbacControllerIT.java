package eu.alboranplus.chinvat.rbac.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.rbac.application.dto.PermissionView;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RbacControllerIT {

    private static final UUID UUID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UUID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID UUID_99 = UUID.fromString("00000000-0000-0000-0000-000000000063");

    private static final TokenPrincipal PROFILE_READER_PRINCIPAL =
        new TokenPrincipal(UUID_1, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"));

    private static final TokenPrincipal RbacManagerPrincipal =
            new TokenPrincipal(
            UUID_2,
                    "rbac-admin@example.com",
                    Set.of("SUPERADMIN"),
                    Set.of("PROFILE:READ", "RBAC:MANAGE", "USERS:MANAGE"));

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RbacFacade rbacFacade;
  @MockitoBean private AuthFacade authFacade;

  @Test
  void getRole_withValidToken_returns200WithRoleData() throws Exception {
    given(rbacFacade.getRole("USER"))
        .willReturn(new RoleView("USER", Set.of("PROFILE:READ")));
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(Optional.of(PROFILE_READER_PRINCIPAL));

    mockMvc
        .perform(
            get("/api/v1/rbac/roles/USER")
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roleName").value("USER"));
  }

  @Test
  void getRole_withoutToken_returns401() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(get("/api/v1/rbac/roles/USER"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getRole_roleNotFound_returns404OrError() throws Exception {
    given(rbacFacade.getRole("UNKNOWN")).willThrow(new RoleNotFoundException("Role not found: UNKNOWN"));
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(Optional.of(PROFILE_READER_PRINCIPAL));

    mockMvc
        .perform(
            get("/api/v1/rbac/roles/UNKNOWN")
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void createPermission_withoutRbacManage_returns403() throws Exception {
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(Optional.of(PROFILE_READER_PRINCIPAL));

    mockMvc
        .perform(
            post("/api/v1/rbac/permissions")
                .header("Authorization", "Bearer valid-token")
                .contentType("application/json")
                .content("""
                    {
                      "code": "RBAC:EXPORT",
                      "description": "Export RBAC data"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

    @Test
    void createPermission_withRbacManage_usesAuthenticatedActor() throws Exception {
        given(authFacade.validateAccessToken("valid-token"))
                .willReturn(Optional.of(RbacManagerPrincipal));
        given(rbacFacade.createPermission("RBAC:EXPORT", "Export RBAC data", "rbac-admin@example.com"))
                .willReturn(new PermissionView("RBAC:EXPORT", "Export RBAC data"));

        mockMvc
                .perform(
                        post("/api/v1/rbac/permissions")
                                .header("Authorization", "Bearer valid-token")
                                .contentType("application/json")
                                .content("""
                                        {
                                            "code": "RBAC:EXPORT",
                                            "description": "Export RBAC data"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("RBAC:EXPORT"));
    }

  @Test
  void assignRoleToUser_withUsersManage_usesAuthenticatedActor() throws Exception {
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(Optional.of(RbacManagerPrincipal));

    mockMvc
        .perform(
            post("/api/v1/rbac/users/99/roles/ADMIN")
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isNoContent());

    then(rbacFacade).should().assignRoleToUser(UUID_99, "ADMIN", "rbac-admin@example.com");
  }

  @Test
  void assignRoleToUser_withoutUsersManage_returns403() throws Exception {
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(Optional.of(PROFILE_READER_PRINCIPAL));

    mockMvc
        .perform(
            post("/api/v1/rbac/users/99/roles/ADMIN")
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isForbidden());

    then(rbacFacade).shouldHaveNoInteractions();
  }

    @Test
    void removeRoleFromUser_withUsersManage_usesAuthenticatedActor() throws Exception {
        given(authFacade.validateAccessToken("valid-token"))
                .willReturn(Optional.of(RbacManagerPrincipal));

        mockMvc
                .perform(
                        delete("/api/v1/rbac/users/99/roles/ADMIN")
                                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent());

        then(rbacFacade).should().removeRoleFromUser(UUID_99, "ADMIN", "rbac-admin@example.com");
    }
}
