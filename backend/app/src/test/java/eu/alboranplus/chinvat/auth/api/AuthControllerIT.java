package eu.alboranplus.chinvat.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.IssuedTokenPair;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web layer integration tests for AuthController. Uses the full Spring context but mocks the
 * facade to avoid any persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthFacade authFacade;

  private static final Instant ACCESS_EXP = Instant.parse("2026-01-01T00:15:00Z");
  private static final Instant REFRESH_EXP = Instant.parse("2026-01-15T00:00:00Z");

  private final AuthResult successResult =
      new AuthResult(
          1L,
          "alice@example.com",
          "Alice",
          Set.of("USER"),
          Set.of("PROFILE:READ"),
          new IssuedTokenPair("access-token", "refresh-token", ACCESS_EXP, REFRESH_EXP));

  @Test
  void login_validCredentials_returns200WithTokens() throws Exception {
    given(authFacade.login(any())).willReturn(successResult);
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"SecretPass1!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("alice@example.com"))
        .andExpect(jsonPath("$.tokens.accessToken").value("access-token"))
        .andExpect(jsonPath("$.tokens.refreshToken").value("refresh-token"));
  }

  @Test
  void login_invalidCredentials_returns401() throws Exception {
    given(authFacade.login(any()))
        .willThrow(
            new eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException(
                "Invalid email or password"));
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"WrongPass1!\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_blankEmail_returns400() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"password\":\"SecretPass1!\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_invalidEmailFormat_returns400() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"SecretPass1!\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refresh_validRefreshToken_returns200() throws Exception {
    given(authFacade.refresh(any())).willReturn(successResult);
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"old-refresh-token\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tokens.accessToken").value("access-token"));
  }

  @Test
  void refresh_missingBody_returns400() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void logout_validRequest_returns204() throws Exception {
    doNothing().when(authFacade).logout(any());
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"accessToken\":\"some-access\",\"refreshToken\":\"some-refresh\"}"))
        .andExpect(status().isNoContent());
  }
}
