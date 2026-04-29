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

  private static final String REGISTER_URL = "/api/v1/auth/register";
  private static final String LOGIN_URL = "/api/v1/auth/login";
  private static final String REFRESH_URL = "/api/v1/auth/refresh";
  private static final String LOGOUT_URL = "/api/v1/auth/logout";
  private static final String ME_URL = "/api/v1/auth/me";
  private static final String SESSIONS_URL = "/api/v1/auth/sessions";
  private static final String PASSWORD_RESET_REQUEST_URL = "/api/v1/auth/password-reset/request";
  private static final String PASSWORD_RESET_CONFIRM_URL = "/api/v1/auth/password-reset/confirm";
  private static final String ROLES_URL = "/api/v1/rbac/roles/USER";

  @Test
  void fullAuthFlow_registerLoginAccessRefreshLogout() throws Exception {

    // 1. Register (returns tokens)
    MvcResult registerResult =
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
            .andExpect(status().isCreated())
            .andReturn();

    String registerBody = registerResult.getResponse().getContentAsString();
    String accessToken = JsonPath.read(registerBody, "$.tokens.accessToken");
    String refreshToken = JsonPath.read(registerBody, "$.tokens.refreshToken");

    assertThat(accessToken).isNotBlank();
    assertThat(refreshToken).isNotBlank();

    // 2. /auth/me should return current profile
    mockMvc
        .perform(get(ME_URL).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(result -> {
          String body = result.getResponse().getContentAsString();
          assertThat((String) JsonPath.read(body, "$.email"))
              .isEqualTo("e2e-user@example.com");
        });

    // 3. List sessions and revoke ACCESS session (token should be rejected afterwards)
    MvcResult sessionsResult =
        mockMvc
            .perform(
                get(SESSIONS_URL).header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn();

    String sessionsBody = sessionsResult.getResponse().getContentAsString();
    Object accessSessionIds =
        JsonPath.read(sessionsBody, "$[?(@.tokenKind=='ACCESS')].sessionId");
    String accessSessionId = ((java.util.List<?>) accessSessionIds).get(0).toString();

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                SESSIONS_URL + "/" + accessSessionId)
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isUnauthorized());

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

    // 6. Password reset (invalidates all sessions)
    String newPassword = "E2EPasswordNew1234!";

    MvcResult resetRequest =
        mockMvc
            .perform(
                post(PASSWORD_RESET_REQUEST_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Debug-Reveal-Reset-Token", "true")
                    .content(
                        """
                        {
                          "email": "e2e-user@example.com"
                        }
                        """))
            .andExpect(status().isAccepted())
            .andReturn();

    String resetBody = resetRequest.getResponse().getContentAsString();
    String resetToken = JsonPath.read(resetBody, "$.resetToken");

    mockMvc
        .perform(
            post(PASSWORD_RESET_CONFIRM_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "resetToken": "%s",
                      "newPassword": "%s"
                    }
                    """.formatted(resetToken, newPassword)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + newAccessToken))
        .andExpect(status().isUnauthorized());

    // 7. Login with new password
    MvcResult reloginResult =
        mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "email": "e2e-user@example.com",
                          "password": "%s"
                        }
                        """.formatted(newPassword)))
            .andExpect(status().isOk())
            .andReturn();

    String reloginBody = reloginResult.getResponse().getContentAsString();
    String finalAccessToken = JsonPath.read(reloginBody, "$.tokens.accessToken");
    String finalRefreshToken = JsonPath.read(reloginBody, "$.tokens.refreshToken");

    // 8. Logout with final token pair
    mockMvc
        .perform(
            post(LOGOUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"accessToken\":\""
                        + finalAccessToken
                        + "\",\"refreshToken\":\""
                        + finalRefreshToken
                        + "\"}"))
        .andExpect(status().isNoContent());

    // 9. Token rejected after logout
    mockMvc
        .perform(
            get(ROLES_URL).header("Authorization", "Bearer " + finalAccessToken))
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
