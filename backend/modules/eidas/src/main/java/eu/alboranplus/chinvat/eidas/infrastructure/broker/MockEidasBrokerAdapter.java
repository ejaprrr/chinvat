package eu.alboranplus.chinvat.eidas.infrastructure.broker;

import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile({"dev", "local"})
@Repository
public class MockEidasBrokerAdapter implements EidasBrokerPort {

  private static final List<EidasProviderView> PROVIDERS =
      List.of(new EidasProviderView("EIDAS_EU", "eIDAS EU", "EU", true));

  @Override
  public List<EidasProviderView> listProviders() {
    return PROVIDERS;
  }

  @Override
  public Optional<EidasProviderView> findEnabledProvider(String providerCode) {
    if (providerCode == null || providerCode.isBlank()) {
      return Optional.empty();
    }
    String normalized = providerCode.toUpperCase(Locale.ROOT);
    return PROVIDERS.stream()
        .filter(EidasProviderView::enabled)
        .filter(provider -> provider.code().toUpperCase(Locale.ROOT).equals(normalized))
        .findFirst();
  }

  @Override
  public EidasLoginView initiateLogin(
      String providerCode, String redirectUri, String state, Instant expiresAt) {
    String authorizationUrl = redirectUri
        + "?state=" + state
        + "&provider_code=" + providerCode
        + "&code=mock-code-" + state
        + "&subject_id=mock-subject-" + state;
    return new EidasLoginView(
        providerCode,
        state,
        authorizationUrl,
        expiresAt);
  }

  @Override
  public EidasBrokerIdentityView exchangeAuthorizationCode(
      String providerCode, String state, String authorizationCode) {
    String subject = "mock-subject-" + state;
    return new EidasBrokerIdentityView(
        subject,
        "high",
        "ES/PI/" + subject,
        "ES/LPI/" + subject,
        "Mock",
        "User",
        "1990-01-01");
  }
}
