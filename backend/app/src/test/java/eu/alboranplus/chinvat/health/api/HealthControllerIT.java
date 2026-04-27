package eu.alboranplus.chinvat.health.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthFacade authFacade;

  @Test
  void health_noAuth_returns200() throws Exception {
    BDDMockito.given(authFacade.validateAccessToken(org.mockito.ArgumentMatchers.any()))
        .willReturn(Optional.empty());

    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").exists());
  }
}
