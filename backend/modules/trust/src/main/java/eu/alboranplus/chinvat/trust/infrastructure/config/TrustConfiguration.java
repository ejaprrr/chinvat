package eu.alboranplus.chinvat.trust.infrastructure.config;

import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import java.io.File;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TrustProperties.class)
public class TrustConfiguration {

  @Bean
  public TrustedListsCertificateSource trustedListsCertificateSource() {
    return new TrustedListsCertificateSource();
  }

  @Bean
  public CommonCertificateVerifier commonCertificateVerifier(
      TrustedListsCertificateSource trustedListsCertificateSource) {
    CommonCertificateVerifier verifier = new CommonCertificateVerifier(true);
    verifier.setTrustedCertSources(trustedListsCertificateSource);
    verifier.setCheckRevocationForUntrustedChains(true);
    return verifier;
  }

  @Bean
  public CommonsDataLoader trustDataLoader(TrustProperties trustProperties) {
    CommonsDataLoader loader = new CommonsDataLoader();
    loader.setTimeoutConnection(trustProperties.getConnectTimeoutMillis());
    loader.setTimeoutConnectionRequest(trustProperties.getConnectTimeoutMillis());
    loader.setTimeoutResponse(trustProperties.getReadTimeoutMillis());
    loader.setTimeoutSocket(trustProperties.getReadTimeoutMillis());
    loader.setConnectionsMaxTotal(50);
    loader.setConnectionsMaxPerRoute(10);
    loader.setUseSystemProperties(true);
    loader.setRedirectsEnabled(true);
    return loader;
  }

  @Bean
  public FileCacheDataLoader trustFileCacheDataLoader(
      CommonsDataLoader trustDataLoader, TrustProperties trustProperties) {
    FileCacheDataLoader loader = new FileCacheDataLoader(trustDataLoader);
    loader.setFileCacheDirectory(new File(System.getProperty("java.io.tmpdir"), "chinvat-dss-cache"));
    loader.setCacheExpirationTime(Math.max(trustProperties.getReadTimeoutMillis(), 3600_000));
    return loader;
  }
}
