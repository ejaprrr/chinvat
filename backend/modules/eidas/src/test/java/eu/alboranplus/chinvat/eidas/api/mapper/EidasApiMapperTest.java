package eu.alboranplus.chinvat.eidas.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.alboranplus.chinvat.eidas.api.dto.EidasCallbackRequest;
import eu.alboranplus.chinvat.eidas.api.dto.EidasLoginRequest;
import eu.alboranplus.chinvat.eidas.application.dto.EidasCallbackView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EidasApiMapperTest {

  private final EidasApiMapper mapper = new EidasApiMapper();

  @Test
  void shouldMapLoginRequestToCommand() {
    var command = mapper.toCommand(new EidasLoginRequest("EIDAS_EU", "https://app.example/callback"));

    assertEquals("EIDAS_EU", command.providerCode());
    assertEquals("https://app.example/callback", command.redirectUri());
  }

  @Test
  void shouldMapCallbackRequestToCommand() {
    var command =
        mapper.toCommand(
            new EidasCallbackRequest("EIDAS_EU", "state-1", "code-1", "subject-1", "high"));

    assertEquals("state-1", command.state());
    assertEquals("subject-1", command.externalSubjectId());
  }

  @Test
  void shouldMapViewsToResponses() {
    Instant now = Instant.parse("2026-05-08T12:00:00Z");
    var loginView = new EidasLoginView("EIDAS_EU", "state-1", "https://idp/auth", now);
    var callbackView =
        new EidasCallbackView(
            "EIDAS_EU", "subject-1", "high", "PENDING_PROFILE", null, true, now);
    var providerView = new EidasProviderView("EIDAS_EU", "eIDAS EU", "EU", true);

    assertEquals("state-1", mapper.toResponse(loginView).state());
    assertEquals("PENDING_PROFILE", mapper.toResponse(callbackView).currentStatus());
    assertTrue(mapper.toResponse(callbackView).profileCompletionRequired());
    assertEquals("EU", mapper.toResponse(providerView).countryCode());
  }
}
