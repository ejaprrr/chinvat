package eu.alboranplus.chinvat.trust.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
class SetPrimaryCertificateCredentialUseCaseTest {

  private static final UUID CREDENTIAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000001f5");
  private static final UUID OWNER_USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000004d");
  private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000058");

  @Mock private CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  private SetPrimaryCertificateCredentialUseCase sut;

  @BeforeEach
  void setUp() {
    sut = new SetPrimaryCertificateCredentialUseCase(certificateCredentialLifecyclePort);
  }

  @Test
  void execute_setsPrimaryForOwnedActiveCredential() {
    Instant now = Instant.now();
    CertificateCredentialView existing =
        new CertificateCredentialView(
            CREDENTIAL_ID,
            OWNER_USER_ID,
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
            "actor",
            now,
            null,
            null,
            false,
            now,
            now);

        given(certificateCredentialLifecyclePort.findById(CREDENTIAL_ID)).willReturn(Optional.of(existing));
    given(certificateCredentialLifecyclePort.save(org.mockito.ArgumentMatchers.any()))
        .willAnswer(invocation -> invocation.getArgument(0));

        CertificateCredentialView result = sut.execute(OWNER_USER_ID, CREDENTIAL_ID);

        verify(certificateCredentialLifecyclePort).clearPrimaryForUser(OWNER_USER_ID);
    assertThat(result.primary()).isTrue();
        assertThat(result.id()).isEqualTo(CREDENTIAL_ID);
  }

  @Test
  void execute_whenCredentialNotOwned_throwsNotFound() {
    Instant now = Instant.now();
    CertificateCredentialView existing =
        new CertificateCredentialView(
            CREDENTIAL_ID,
            OTHER_USER_ID,
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
            "actor",
            now,
            null,
            null,
            false,
            now,
            now);

    given(certificateCredentialLifecyclePort.findById(CREDENTIAL_ID)).willReturn(Optional.of(existing));

    assertThatThrownBy(() -> sut.execute(OWNER_USER_ID, CREDENTIAL_ID))
        .isInstanceOf(CertificateCredentialNotFoundException.class);
  }
}
