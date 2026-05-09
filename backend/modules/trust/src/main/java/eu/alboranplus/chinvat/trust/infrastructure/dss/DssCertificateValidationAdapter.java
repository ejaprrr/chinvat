package eu.alboranplus.chinvat.trust.infrastructure.dss;

import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateValidationPort;
import eu.alboranplus.chinvat.trust.domain.exception.TrustValidationException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.KeyUsageBit;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DssCertificateValidationAdapter implements CertificateValidationPort {

  private final TrustedListsCertificateSource trustedListsCertificateSource;

  public DssCertificateValidationAdapter(
      TrustedListsCertificateSource trustedListsCertificateSource) {
    this.trustedListsCertificateSource = trustedListsCertificateSource;
  }

  @Override
  public CertificateValidationResult validate(String certificatePem) {
    try {
      CertificateToken certificateToken =
          DSSUtils.loadCertificate(
              new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8)));
      Instant now = Instant.now();
      List<String> keyUsageFlags =
          certificateToken.getKeyUsageBits().stream().map(KeyUsageBit::name).sorted().toList();
      boolean trustedIssuer = trustedListsCertificateSource.isTrusted(certificateToken);

      return new CertificateValidationResult(
          DSSUtils.toHex(certificateToken.getDigest(DigestAlgorithm.SHA256)),
          certificateToken.getSubject().getRFC2253(),
          certificateToken.getIssuer().getRFC2253(),
          certificateToken.getSerialNumber().toString(16).toUpperCase(),
          certificateToken.getNotBefore().toInstant(),
          certificateToken.getNotAfter().toInstant(),
          certificateToken.isValidOn(java.util.Date.from(now)),
          trustedIssuer,
          trustedIssuer ? trustedListsCertificateSource.getCertificateSourceType().name() : "UNTRUSTED",
          keyUsageFlags,
          now);
    } catch (Exception exception) {
      throw new TrustValidationException("Certificate validation failed", exception);
    }
  }
}
