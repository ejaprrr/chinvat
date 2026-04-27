package eu.alboranplus.chinvat.rbac.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import eu.alboranplus.chinvat.rbac.application.dto.RoleView;
import eu.alboranplus.chinvat.rbac.application.facade.RbacFacade;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RbacControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private RbacFacade rbacFacade;
  @MockitoBean private AuthFacade authFacade;

  @Test
  void getRole_withValidToken_returns200WithRoleData() throws Exception {
    given(rbacFacade.getRole("USER"))
        .willReturn(new RoleView("USER", Set.of("PROFILE:READ")));
    given(authFacade.validateAccessToken("valid-token"))
        .willReturn(
            Optional.of(new TokenPrincipal(1L, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"))));

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
        .willReturn(
            Optional.of(new TokenPrincipal(1L, "alice@example.com", Set.of("USER"), Set.of("PROFILE:READ"))));

    mockMvc
        .perform(
            get("/api/v1/rbac/roles/UNKNOWN")
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().is4xxClientError());
  }
}
