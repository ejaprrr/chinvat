package eu.alboranplus.chinvat.eidas.infrastructure.mock;

import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerCallbackRequest;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerCallbackResponse;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerLoginRequest;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerLoginResponse;
import eu.alboranplus.chinvat.eidas.infrastructure.broker.dto.BrokerProviderResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "local"})
@RestController
@RequestMapping("/api/v1/auth/eidas/mock-broker")
public class LocalEidasBrokerMockController {

  @GetMapping("/providers")
  public ResponseEntity<List<BrokerProviderResponse>> providers() {
    return ResponseEntity.ok(
        List.of(new BrokerProviderResponse("EIDAS_EU", "eIDAS EU", "EU", true)));
  }

  @PostMapping("/login")
  public ResponseEntity<BrokerLoginResponse> login(@RequestBody BrokerLoginRequest request) {
    String expiresAt =
      request.expiresAt() == null ? Instant.now().plusSeconds(600).toString() : request.expiresAt();
    String authorizationUrl = request.redirectUri()
        + "?state=" + request.state()
        + "&provider_code=" + request.providerCode()
        + "&code=mock-code-" + request.state()
        + "&subject_id=mock-subject-" + request.state();
    return ResponseEntity.ok(new BrokerLoginResponse(authorizationUrl, expiresAt));
  }

  @PostMapping("/callback")
  public ResponseEntity<BrokerCallbackResponse> callback(@RequestBody BrokerCallbackRequest request) {
    String subject = "mock-subject-" + request.state();
    return ResponseEntity.ok(
        new BrokerCallbackResponse(
            subject,
            "high",
            "ES/PI/" + subject,
            "ES/LPI/" + subject,
            "Mock",
            "User",
            "1990-01-01"));
  }
}
