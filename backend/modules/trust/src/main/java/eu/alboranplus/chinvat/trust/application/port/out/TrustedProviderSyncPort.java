package eu.alboranplus.chinvat.trust.application.port.out;

import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;

public interface TrustedProviderSyncPort {
  TrustedProviderSyncResult synchronize(boolean onlineRefresh);
}
