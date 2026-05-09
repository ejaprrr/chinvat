package eu.alboranplus.chinvat.eidas.infrastructure.adapter;

import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasProviderRegistryPort;
import eu.alboranplus.chinvat.eidas.infrastructure.config.EidasProperties;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigurationEidasProviderRegistryAdapter implements EidasProviderRegistryPort {

  private final EidasProperties eidasProperties;

  public ConfigurationEidasProviderRegistryAdapter(EidasProperties eidasProperties) {
    this.eidasProperties = eidasProperties;
  }

  @Override
  public List<EidasProviderView> listProviders() {
    return eidasProperties.getProviders().stream()
        .map(
            provider ->
                new EidasProviderView(
                    provider.getCode(),
                    provider.getDisplayName(),
                    provider.getCountryCode(),
                    provider.isEnabled()))
        .toList();
  }

  @Override
  public Optional<EidasProviderView> findEnabledProvider(String providerCode) {
    if (providerCode == null || providerCode.isBlank()) {
      return Optional.empty();
    }
    String normalized = providerCode.toUpperCase(Locale.ROOT);
    return listProviders().stream()
        .filter(EidasProviderView::enabled)
        .filter(provider -> provider.code().toUpperCase(Locale.ROOT).equals(normalized))
        .findFirst();
  }
}
