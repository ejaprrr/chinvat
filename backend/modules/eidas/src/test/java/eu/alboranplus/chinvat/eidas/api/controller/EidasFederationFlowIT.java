package eu.alboranplus.chinvat.eidas.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.eidas.EidasTestApplication;
import eu.alboranplus.chinvat.eidas.api.exception.EidasApiExceptionHandler;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import eu.alboranplus.chinvat.eidas.infrastructure.persistence.jpa.ExternalIdentityJpaRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.Mockito;

@SpringBootTest(classes = EidasTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(EidasFederationFlowIT.TestMocks.class)
@Transactional
class EidasFederationFlowIT {

  private static final AtomicReference<String> LOGIN_REQUEST_BODY = new AtomicReference<>();
  private static final AtomicReference<String> CALLBACK_REQUEST_BODY = new AtomicReference<>();
  private static HttpServer brokerServer;
  private static String brokerBaseUrl;

  private MockMvc mockMvc;
  @Autowired private EidasController eidasController;
  @Autowired private EidasApiExceptionHandler eidasApiExceptionHandler;
  @Autowired private ExternalIdentityJpaRepository externalIdentityJpaRepository;
  @Autowired private EidasStatePort eidasStatePort;

  static {
    startBrokerServer();
  }

  @BeforeEach
  void setUpMockMvc() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(eidasController)
            .setControllerAdvice(eidasApiExceptionHandler)
            .build();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("chinvat.eidas.broker-base-url", () -> brokerBaseUrl);
    registry.add("chinvat.eidas.broker-providers-path", () -> "/api/v1/providers");
    registry.add("chinvat.eidas.broker-login-path", () -> "/api/v1/login");
    registry.add("chinvat.eidas.broker-callback-path", () -> "/api/v1/callback");
  }

  @BeforeAll
  static void resetRequestCapture() {
    LOGIN_REQUEST_BODY.set(null);
    CALLBACK_REQUEST_BODY.set(null);
  }

  @AfterAll
  static void stopBrokerServer() {
    if (brokerServer != null) {
      brokerServer.stop(0);
    }
  }

  @Test
  void fullFederationFlow_persistsExternalIdentityAndUsesBroker() throws Exception {
    Instant expiresAt = Instant.parse("2026-05-08T12:10:00Z");
    given(eidasStatePort.consume("state-1"))
        .willReturn(Optional.of(new EidasStatePort.EidasStateRecord("state-1", "EIDAS_EU", expiresAt)));

    mockMvc
        .perform(
            post("/api/v1/auth/eidas/login")
                .contentType("application/json")
                .content("{\"providerCode\":\"EIDAS_EU\",\"redirectUri\":\"https://app.example/callback\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.providerCode").value("EIDAS_EU"))
        .andExpect(jsonPath("$.authorizationUrl").value("https://broker.example/authorize"));

    assertThat(LOGIN_REQUEST_BODY.get()).contains("\"providerCode\":\"EIDAS_EU\"");
    assertThat(LOGIN_REQUEST_BODY.get()).contains("\"state\":");

    mockMvc
        .perform(
            post("/api/v1/auth/eidas/callback")
                .contentType("application/json")
                .content(
                    "{\"providerCode\":\"EIDAS_EU\",\"state\":\"state-1\",\"authorizationCode\":\"auth-code-1\",\"externalSubjectId\":\"ignored-by-broker\",\"levelOfAssurance\":\"high\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.providerCode").value("EIDAS_EU"))
        .andExpect(jsonPath("$.externalSubjectId").value("subject-123"))
        .andExpect(jsonPath("$.profileCompletionRequired").value(true));

    assertThat(CALLBACK_REQUEST_BODY.get()).contains("\"authorizationCode\":\"auth-code-1\"");
    assertThat(externalIdentityJpaRepository.findAll()).hasSize(1);
    assertThat(externalIdentityJpaRepository.findAll().get(0).getExternalSubjectId())
        .isEqualTo("subject-123");
  }

  @Configuration
  static class TestMocks {

    @Bean
    EidasStatePort eidasStatePort() {
      return Mockito.mock(EidasStatePort.class);
    }

    @Bean
    AuditFacade auditFacade() {
      return Mockito.mock(AuditFacade.class);
    }
  }

  private static void startBrokerServer() {
    try {
      brokerServer = HttpServer.create(new InetSocketAddress(0), 0);
      brokerServer.createContext(
          "/api/v1/providers",
          exchange -> respond(exchange, 200, "[{'code':'EIDAS_EU','displayName':'eIDAS EU','countryCode':'EU','enabled':true}]".replace('\'', '"')));
      brokerServer.createContext(
          "/api/v1/login",
          exchange -> {
            LOGIN_REQUEST_BODY.set(readBody(exchange));
            respond(
                exchange,
                200,
                "{\"authorizationUrl\":\"https://broker.example/authorize\",\"expiresAt\":\"2026-05-08T12:10:00Z\"}");
          });
      brokerServer.createContext(
          "/api/v1/callback",
          exchange -> {
            CALLBACK_REQUEST_BODY.set(readBody(exchange));
            respond(
                exchange,
                200,
                "{\"externalSubjectId\":\"subject-123\",\"levelOfAssurance\":\"high\",\"personIdentifier\":\"PID-123\",\"legalPersonIdentifier\":\"LPID-123\",\"firstName\":\"Alice\",\"familyName\":\"Smith\",\"dateOfBirth\":\"1990-01-01\"}");
          });
      brokerServer.start();
      brokerBaseUrl = "http://127.0.0.1:" + brokerServer.getAddress().getPort();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to start broker test server", exception);
    }
  }

  private static String readBody(HttpExchange exchange) throws IOException {
    try (InputStream inputStream = exchange.getRequestBody()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.close();
  }
}