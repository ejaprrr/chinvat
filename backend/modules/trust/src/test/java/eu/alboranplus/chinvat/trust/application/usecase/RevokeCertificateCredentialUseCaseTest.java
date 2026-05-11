package eu.alboranplus.chinvat.trust.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokeCertificateCredentialUseCaseTest {

  private static final UUID CREDENTIAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000001f5");
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000004d");

  @Mock private CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  private RevokeCertificateCredentialUseCase sut;

  @BeforeEach
  void setUp() {
    sut = new RevokeCertificateCredentialUseCase(certificateCredentialLifecyclePort);
  }

  @Test
  void execute_marksCredentialRevokedAndStoresMetadata() {
    Instant now = Instant.now();
    CertificateCredentialView existing =
        new CertificateCredentialView(
            CREDENTIAL_ID,
            USER_ID,
            "FNMT",
            "CLIENT_TLS",
            "TRUSTED",
            "ACTIVE",
            "high",
            "PROFILE_SELF_SERVICE",
            "pem",
            "thumb",
            "subject",
            "issuer",
            "serial",
            now.minusSeconds(60),
            now.plusSeconds(3600),
            "approver",
            now.minusSeconds(30),
            null,
            null,
            true,
            now,
            now);

        given(certificateCredentialLifecyclePort.findById(CREDENTIAL_ID)).willReturn(Optional.of(existing));
    given(certificateCredentialLifecyclePort.save(org.mockito.ArgumentMatchers.any()))
        .willAnswer(invocation -> invocation.getArgument(0));

        CertificateCredentialView result = sut.execute(CREDENTIAL_ID, "maria@example.com", "ROTATED");

        assertThat(result.id()).isEqualTo(CREDENTIAL_ID);
    assertThat(result.revocationStatus()).isEqualTo("REVOKED");
    assertThat(result.revokedBy()).isEqualTo("maria@example.com");
    assertThat(result.revokedAt()).isNotNull();
    assertThat(result.primary()).isFalse();
  }

  @Test
  void execute_whenCredentialMissing_throwsNotFound() {
    given(certificateCredentialLifecyclePort.findById(CREDENTIAL_ID)).willReturn(Optional.empty());

    assertThatThrownBy(() -> sut.execute(CREDENTIAL_ID, "maria@example.com", "ROTATED"))
        .isInstanceOf(CertificateCredentialNotFoundException.class)
        .hasMessageContaining("Certificate credential not found");
  }
}
