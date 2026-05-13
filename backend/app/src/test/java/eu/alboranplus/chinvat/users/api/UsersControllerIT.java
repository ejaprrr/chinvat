package eu.alboranplus.chinvat.users.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UsersFacade usersFacade;
  @MockitoBean private AuthFacade authFacade;

  private static final UUID UUID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final String VALID_REQUEST =
      """
      {
        "username": "alice",
        "fullName": "Alice Smith",
        "email": "alice@example.com",
        "password": "SecurePassword1!",
        "userType": "INDIVIDUAL",
        "defaultLanguage": "en"
      }
      """;

  private final UserView createdUser =
      new UserView(UUID_1, "alice", "Alice Smith", null, "alice@example.com",
          UserType.INDIVIDUAL, AccessLevel.NORMAL, null, null, null, null, "en");

  @Test
  void createUser_validRequest_returns201WithUserBody() throws Exception {
    given(usersFacade.createUser(any())).willReturn(createdUser);
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(UUID_1.toString()))
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(jsonPath("$.userType").value("INDIVIDUAL"))
        .andExpect(jsonPath("$.accessLevel").value("NORMAL"));
  }

  @Test
  void createUser_duplicateEmail_returns409() throws Exception {
    given(usersFacade.createUser(any())).willThrow(new UserAlreadyExistsException("exists"));
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.errorCode").value("USR-409-001"))
      .andExpect(jsonPath("$.messageKey").value("error.users.already-exists"))
      .andExpect(jsonPath("$.message").value("exists"))
      .andExpect(jsonPath("$.timestamp").isString())
      .andExpect(jsonPath("$.path").value("/api/v1/users"))
      .andExpect(jsonPath("$.details").isArray());
  }

  @Test
  void createUser_missingEmail_returns400() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "alice",
                      "fullName": "Alice Smith",
                      "password": "SecurePassword1!",
                      "userType": "INDIVIDUAL",
                      "defaultLanguage": "en"
                    }
                    """))
                  .andExpect(status().isBadRequest())
                  .andExpect(jsonPath("$.errorCode").value("API-400-001"))
                  .andExpect(jsonPath("$.messageKey").value("error.common.validation-failed"))
                  .andExpect(jsonPath("$.timestamp").isString())
                  .andExpect(jsonPath("$.path").value("/api/v1/users"))
                  .andExpect(jsonPath("$.details[0].field").exists());
  }

  @Test
  void createUser_passwordTooShort_returns400() throws Exception {
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "alice",
                      "fullName": "Alice Smith",
                      "email": "alice@example.com",
                      "password": "short",
                      "userType": "INDIVIDUAL",
                      "defaultLanguage": "en"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createUser_noAuthRequired_unauthenticatedRequestSucceeds() throws Exception {
    given(usersFacade.createUser(any())).willReturn(createdUser);
    given(authFacade.validateAccessToken(any())).willReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
        .andExpect(status().isCreated());
  }
}
