package eu.alboranplus.chinvat.eidas.application.port.out;

import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import java.util.List;
import java.util.Optional;

public interface EidasProviderRegistryPort {
  List<EidasProviderView> listProviders();

  Optional<EidasProviderView> findEnabledProvider(String providerCode);
}
