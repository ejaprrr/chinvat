package eu.alboranplus.chinvat.trust.application.usecase;

import eu.alboranplus.chinvat.trust.application.command.SyncTrustedProvidersCommand;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import eu.alboranplus.chinvat.trust.application.port.out.TrustedProviderSyncPort;
import org.springframework.stereotype.Service;

@Service
public class SyncTrustedProvidersUseCase {

  private final TrustedProviderSyncPort trustedProviderSyncPort;

  public SyncTrustedProvidersUseCase(TrustedProviderSyncPort trustedProviderSyncPort) {
    this.trustedProviderSyncPort = trustedProviderSyncPort;
  }

  public TrustedProviderSyncResult execute(SyncTrustedProvidersCommand command) {
    return trustedProviderSyncPort.synchronize(command.onlineRefresh());
  }
}
