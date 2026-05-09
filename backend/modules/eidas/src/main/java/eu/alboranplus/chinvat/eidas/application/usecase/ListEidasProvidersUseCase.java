package eu.alboranplus.chinvat.eidas.application.usecase;

import eu.alboranplus.chinvat.eidas.application.dto.EidasProviderView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListEidasProvidersUseCase {

  private final EidasBrokerPort eidasBrokerPort;

  public ListEidasProvidersUseCase(EidasBrokerPort eidasBrokerPort) {
    this.eidasBrokerPort = eidasBrokerPort;
  }

  public List<EidasProviderView> execute() {
    return eidasBrokerPort.listProviders();
  }
}
