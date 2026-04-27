package eu.alboranplus.chinvat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end test covering the full auth flow: register → login → access protected resource →
 * refresh → logout → verify token rejected. Uses the "local" Spring profile (H2 in-memory,
 * DDL create-drop).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext
class AuthFlowE2ETest {

  @Autowired private MockMvc mockMvc;

  private static final String REGISTER_URL = "/api/v1/users";
  private static final String LOGIN_URL = "/api/v1/auth/login";
  private static final String REFRESH_URL = "/api/v1/auth/refresh";
  private static final String LOGOUT_URL = "/api/v1/auth/logout";
  private static final String ROLES_URL = "/api/v1/rbac/roles/USER";

  @Test
  void fullAuthFlow_registerLoginAccessRefreshLogout() throws Exception {

    // 1. Register
    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "e2e-user",
                      "fullName": "E2E User",
                      "email": "e2e-user@example.com",
                      "password": "E2EPassword1234!",
                      "userType": "INDIVIDUAL",
                      "defaultLanguage": "en"
                    }
                    """))
        .andExpect(status().isCreated());

    // 2. Login
    MvcResult loginResult =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "email": "e2e-user@example.com",
                          "password": "E2EPassword1234!"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn();

    String loginBody = loginResult.getResponse().getContentAsString();
    String accessToken = JsonPath.read(loginBody, "$.tokens.accessToken");
    String refreshToken = JsonPath.read(loginBody, "$.tokens.refreshToken");

    assertThat(accessToken).isNotBlank();
    assertThat(refreshToken).isNotBlank();

    // 3. Access protected resource with valid token
    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // 4. Refresh token rotation
    MvcResult refreshResult =
        mockMvc
            .perform(
                post(REFRESH_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

    String refreshBody = refreshResult.getResponse().getContentAsString();
    String newAccessToken = JsonPath.read(refreshBody, "$.tokens.accessToken");
    String newRefreshToken = JsonPath.read(refreshBody, "$.tokens.refreshToken");

    assertThat(newAccessToken).isNotBlank().isNotEqualTo(accessToken);
    assertThat(newRefreshToken).isNotBlank().isNotEqualTo(refreshToken);

    // 5. New access token works on protected resource
    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + newAccessToken))
        .andExpect(status().isOk());

    // 6. Logout with new token pair
    mockMvc
        .perform(
            post(LOGOUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"accessToken\":\""
                        + newAccessToken
                        + "\",\"refreshToken\":\""
                        + newRefreshToken
                        + "\"}"))
        .andExpect(status().isNoContent());

    // 7. Token rejected after logout
    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + newAccessToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_wrongPassword_returns401() throws Exception {
    // Register a fresh user
    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "e2e-bad-pass",
                      "fullName": "BadPassUser",
                      "email": "e2e-bad-pass@example.com",
                      "password": "CorrectPassword1!",
                      "userType": "INDIVIDUAL",
                      "defaultLanguage": "en"
                    }
                    """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "e2e-bad-pass@example.com",
                      "password": "WrongPassword99!"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(get(ROLES_URL)).andExpect(status().isUnauthorized());
  }

  @Test
  void health_endpoint_isPublic() throws Exception {
    mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
  }
}
