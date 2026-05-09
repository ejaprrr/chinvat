package eu.alboranplus.chinvat.trust.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import eu.alboranplus.chinvat.trust.application.command.BindCertificateCredentialCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateCredentialLifecyclePort;
import eu.alboranplus.chinvat.trust.application.port.out.CertificateValidationPort;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BindCertificateCredentialUseCaseTest {

  @Mock private CertificateValidationPort certificateValidationPort;
  @Mock private CertificateCredentialLifecyclePort certificateCredentialLifecyclePort;

  private BindCertificateCredentialUseCase sut;

  @BeforeEach
  void setUp() {
    sut =
        new BindCertificateCredentialUseCase(
            certificateValidationPort, certificateCredentialLifecyclePort);
  }

  @Test
  void execute_whenIssuerTrusted_persistsTrustedActiveCredential() {
    BindCertificateCredentialCommand command =
        new BindCertificateCredentialCommand(
            77L, "FNMT", "PROFILE_SELF_SERVICE", "high", "pem-data");

    Instant now = Instant.now();
    CertificateValidationResult validation =
        new CertificateValidationResult(
            "thumb-1",
            "subject",
            "issuer",
            "serial",
            now.minusSeconds(120),
            now.plusSeconds(3600),
            true,
            true,
            "TL",
            List.of("digitalSignature"),
            now);

    given(certificateValidationPort.validate("pem-data")).willReturn(validation);
    given(certificateCredentialLifecyclePort.save(org.mockito.ArgumentMatchers.any()))
        .willAnswer(invocation -> invocation.getArgument(0));

    CertificateCredentialView result = sut.execute(command, "maria@example.com");

    verify(certificateValidationPort).validate("pem-data");
    assertThat(result.userId()).isEqualTo(77L);
    assertThat(result.providerCode()).isEqualTo("FNMT");
    assertThat(result.trustStatus()).isEqualTo("TRUSTED");
    assertThat(result.revocationStatus()).isEqualTo("ACTIVE");
    assertThat(result.primary()).isFalse();
    assertThat(result.approvedBy()).isEqualTo("maria@example.com");
  }

  @Test
  void execute_whenIssuerNotTrusted_marksCredentialUntrusted() {
    BindCertificateCredentialCommand command =
        new BindCertificateCredentialCommand(77L, "FNMT", "PROFILE_SELF_SERVICE", "high", "pem-data");

    Instant now = Instant.now();
    CertificateValidationResult validation =
        new CertificateValidationResult(
            "thumb-2",
            "subject",
            "issuer",
            "serial",
            now.minusSeconds(120),
            now.plusSeconds(3600),
            true,
            false,
            "MANUAL",
            List.of(),
            now);

    given(certificateValidationPort.validate("pem-data")).willReturn(validation);
    given(certificateCredentialLifecyclePort.save(org.mockito.ArgumentMatchers.any()))
        .willAnswer(invocation -> invocation.getArgument(0));

    CertificateCredentialView result = sut.execute(command, "maria@example.com");

    assertThat(result.trustStatus()).isEqualTo("UNTRUSTED");
    assertThat(result.thumbprintSha256()).isEqualTo("thumb-2");
  }
}
