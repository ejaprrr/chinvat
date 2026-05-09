package eu.alboranplus.chinvat.eidas.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.eidas.api.mapper.EidasApiMapper;
import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.command.InitiateEidasLoginCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.facade.EidasFacade;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EidasControllerTest {

  @Mock private EidasFacade eidasFacade;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new EidasController(eidasFacade, new EidasApiMapper()))
            .build();
  }

  @Test
  void initiateLogin_returnsAuthorizationUrlAndState() throws Exception {
    Instant expiresAt = Instant.parse("2026-05-08T10:20:00Z");
    given(eidasFacade.initiateLogin(any(InitiateEidasLoginCommand.class)))
        .willReturn(new EidasLoginView("EIDAS_EU", "state-1", "https://idp/auth", expiresAt));

    mockMvc
        .perform(
            post("/api/v1/auth/eidas/login")
                .contentType("application/json")
                .content("{\"providerCode\":\"EIDAS_EU\",\"redirectUri\":\"https://app.example/callback\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.providerCode").value("EIDAS_EU"))
        .andExpect(jsonPath("$.state").value("state-1"))
        .andExpect(jsonPath("$.authorizationUrl").value("https://idp/auth"));
  }

  @Test
  void callback_returnsLinkingDecision() throws Exception {
    Instant now = Instant.parse("2026-05-08T10:20:00Z");
    given(eidasFacade.handleCallback(any(HandleEidasCallbackCommand.class)))
        .willReturn(
        new EidasCallbackView(
          "EIDAS_EU", "subject-1", "high", "PENDING_PROFILE", null, true, now));

    mockMvc
        .perform(
            post("/api/v1/auth/eidas/callback")
                .contentType("application/json")
                .content(
                    "{\"providerCode\":\"EIDAS_EU\",\"state\":\"state-1\",\"authorizationCode\":\"code-1\",\"externalSubjectId\":\"subject-1\",\"levelOfAssurance\":\"high\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.providerCode").value("EIDAS_EU"))
        .andExpect(jsonPath("$.currentStatus").value("PENDING_PROFILE"))
        .andExpect(jsonPath("$.profileCompletionRequired").value(true));
  }

  @Test
  void providers_returnsConfiguredProviders() throws Exception {
    given(eidasFacade.listProviders())
        .willReturn(List.of(new EidasProviderView("EIDAS_EU", "eIDAS EU", "EU", true)));

    mockMvc
        .perform(get("/api/v1/auth/eidas/providers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].code").value("EIDAS_EU"));
  }
}
