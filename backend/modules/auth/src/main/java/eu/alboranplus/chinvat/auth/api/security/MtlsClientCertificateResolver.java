package eu.alboranplus.chinvat.auth.api.security;

import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MtlsClientCertificateResolver {

  private final String sharedSecret;

  public MtlsClientCertificateResolver(
      @Value("${app.security.fnmt.proxy-shared-secret}") String sharedSecret) {
    this.sharedSecret = sharedSecret;
  }

  public String resolveThumbprintSha256(HttpServletRequest request) {
    String proxySecret = request.getHeader("X-Internal-Proxy-Auth");
    if (proxySecret == null || !proxySecret.equals(sharedSecret)) {
      throw new InvalidAuthenticationException("FNMT login is only available through the trusted gateway");
    }

    String verifyHeader = request.getHeader("X-SSL-Client-Verify");
    if (!"SUCCESS".equalsIgnoreCase(verifyHeader)) {
      throw new InvalidAuthenticationException("A valid client certificate is required");
    }

    String escapedCertificate = request.getHeader("X-SSL-Client-Cert");
    if (escapedCertificate == null || escapedCertificate.isBlank()) {
      throw new InvalidAuthenticationException("The client certificate was not forwarded by the gateway");
    }

    try {
      String pem = URLDecoder.decode(escapedCertificate, StandardCharsets.UTF_8);
      X509Certificate certificate =
          (X509Certificate)
              CertificateFactory.getInstance("X.509")
                  .generateCertificate(new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
      return hexSha256(certificate.getEncoded());
    } catch (IllegalArgumentException | CertificateException exception) {
      throw new InvalidAuthenticationException("The forwarded client certificate is invalid");
    }
  }

  private static String hexSha256(byte[] data) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
      StringBuilder builder = new StringBuilder(digest.length * 2);
      for (byte value : digest) {
        builder.append(String.format("%02X", value));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }
}