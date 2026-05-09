package eu.alboranplus.chinvat.trust.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.alboranplus.chinvat.trust.api.dto.SyncTrustedProvidersRequest;
import eu.alboranplus.chinvat.trust.api.dto.ValidateCertificateRequest;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.dto.TrustedProviderSyncResult;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrustApiMapperTest {

  private final TrustApiMapper mapper = new TrustApiMapper();

  @Test
  void shouldMapValidateCommand() {
    var command = mapper.toCommand(new ValidateCertificateRequest("pem", true));

    assertEquals("pem", command.certificatePem());
    assertTrue(command.refreshTrustedProvidersBeforeValidation());
  }

  @Test
  void shouldMapSyncCommandWithDefaultOnlineRefresh() {
    var command = mapper.toCommand((SyncTrustedProvidersRequest) null);

    assertTrue(command.onlineRefresh());
  }

  @Test
  void shouldMapResponses() {
    Instant now = Instant.parse("2026-05-08T10:15:30Z");
    var validation =
        new CertificateValidationResult(
            "thumb",
            "subject",
            "issuer",
            "ABC123",
            now,
            now,
            true,
            true,
            "TRUSTED_LISTS",
            List.of("DIGITAL_SIGNATURE"),
            now);
    var sync = new TrustedProviderSyncResult("https://example.test/lotl.xml", 1, 27, 540, now);

    assertEquals("thumb", mapper.toResponse(validation).thumbprintSha256());
    assertEquals(540, mapper.toResponse(sync).trustedCertificates());
  }
}
