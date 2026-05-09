package eu.alboranplus.chinvat.trust.infrastructure.dss;

import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import eu.alboranplus.chinvat.trust.application.port.out.TrustedProviderSyncPort;
import eu.alboranplus.chinvat.trust.domain.exception.TrustProviderSyncException;
import eu.alboranplus.chinvat.trust.infrastructure.config.TrustProperties;
import eu.europa.esig.dss.model.tsl.TLValidationJobSummary;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class DssTrustedProviderSyncAdapter implements TrustedProviderSyncPort {

  private final TrustedListsCertificateSource trustedListsCertificateSource;
  private final FileCacheDataLoader trustFileCacheDataLoader;
  private final TrustProperties trustProperties;
  private final Lock synchronizationLock = new ReentrantLock();

  public DssTrustedProviderSyncAdapter(
      TrustedListsCertificateSource trustedListsCertificateSource,
      FileCacheDataLoader trustFileCacheDataLoader,
      TrustProperties trustProperties) {
    this.trustedListsCertificateSource = trustedListsCertificateSource;
    this.trustFileCacheDataLoader = trustFileCacheDataLoader;
    this.trustProperties = trustProperties;
  }

  @Override
  public TrustedProviderSyncResult synchronize(boolean onlineRefresh) {
    synchronizationLock.lock();
    try {
      LOTLSource lotlSource = new LOTLSource();
      lotlSource.setUrl(trustProperties.getEuLotlUrl());

      TLValidationJob job = new TLValidationJob();
      job.setDebug(trustProperties.isDebug());
      job.setOnlineDataLoader(trustFileCacheDataLoader);
      job.setOfflineDataLoader(trustFileCacheDataLoader);
      job.setListOfTrustedListSources(lotlSource);
      job.setTrustedListCertificateSource(trustedListsCertificateSource);

      if (onlineRefresh) {
        job.onlineRefresh();
      } else {
        job.offlineRefresh();
      }

      TLValidationJobSummary summary = job.getSummary();
      return new TrustedProviderSyncResult(
          trustProperties.getEuLotlUrl(),
          summary == null ? 0 : summary.getNumberOfProcessedLOTLs(),
          summary == null ? 0 : summary.getNumberOfProcessedTLs(),
          trustedListsCertificateSource.getNumberOfCertificates(),
          Instant.now());
    } catch (Exception exception) {
      throw new TrustProviderSyncException("Trusted provider synchronization failed", exception);
    } finally {
      synchronizationLock.unlock();
    }
  }
}
