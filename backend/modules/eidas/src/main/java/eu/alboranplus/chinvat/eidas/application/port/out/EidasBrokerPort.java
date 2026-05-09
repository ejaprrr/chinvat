package eu.alboranplus.chinvat.eidas.application.port.out;

import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasLoginView;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EidasBrokerPort {

  List<EidasProviderView> listProviders();

  Optional<EidasProviderView> findEnabledProvider(String providerCode);

  EidasLoginView initiateLogin(String providerCode, String redirectUri, String state, Instant expiresAt);

  EidasBrokerIdentityView exchangeAuthorizationCode(
      String providerCode, String state, String authorizationCode);
}