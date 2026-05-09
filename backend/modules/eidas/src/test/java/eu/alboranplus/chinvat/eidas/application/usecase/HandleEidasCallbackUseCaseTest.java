package eu.alboranplus.chinvat.eidas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.eidas.application.command.HandleEidasCallbackCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasBrokerIdentityView;
import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasBrokerPort;
import eu.alboranplus.chinvat.eidas.application.port.out.EidasStatePort;
import eu.alboranplus.chinvat.eidas.application.port.out.ExternalIdentityLifecyclePort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasInvalidStateException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HandleEidasCallbackUseCaseTest {

  @Mock private EidasBrokerPort eidasBrokerPort;
  @Mock private EidasStatePort eidasStatePort;
  @Mock private ExternalIdentityLifecyclePort externalIdentityLifecyclePort;

  private HandleEidasCallbackUseCase sut;

  @BeforeEach
  void setUp() {
  sut = new HandleEidasCallbackUseCase(
    eidasBrokerPort, eidasStatePort, externalIdentityLifecyclePort);
  }

  @Test
  void execute_whenStateMatches_returnsCallbackView() {
    Instant expiresAt = Instant.now().plusSeconds(600);
    given(eidasStatePort.consume("state-1"))
        .willReturn(Optional.of(new EidasStatePort.EidasStateRecord("state-1", "EIDAS_EU", expiresAt)));
  given(eidasBrokerPort.exchangeAuthorizationCode("EIDAS_EU", "state-1", "auth-code"))
    .willReturn(
      new EidasBrokerIdentityView(
        "external-subject", "high", "person-1", "legal-1", "Alice", "Smith", "1990-01-01"));
  given(externalIdentityLifecyclePort.findLinkedUserId("EIDAS_EU", "external-subject"))
    .willReturn(Optional.empty());
  given(externalIdentityLifecyclePort.save(org.mockito.ArgumentMatchers.any(ExternalIdentityView.class)))
    .willAnswer(invocation -> invocation.getArgument(0, ExternalIdentityView.class));

    var result =
        sut.execute(
            new HandleEidasCallbackCommand(
                "EIDAS_EU", "state-1", "auth-code", "external-subject", "high"));

    assertThat(result.providerCode()).isEqualTo("EIDAS_EU");
    assertThat(result.profileCompletionRequired()).isTrue();
    assertThat(result.currentStatus()).isEqualTo("PENDING_PROFILE");
    assertThat(result.externalSubjectId()).isEqualTo("external-subject");
    assertThat(result.linkedUserId()).isNull();
  }

  @Test
  void execute_whenStateMissing_throwsInvalidState() {
    given(eidasStatePort.consume("state-1")).willReturn(Optional.empty());

    assertThatThrownBy(
        () ->
            sut.execute(
                new HandleEidasCallbackCommand(
                    "EIDAS_EU", "state-1", "auth-code", "external-subject", "high")))
        .isInstanceOf(EidasInvalidStateException.class)
        .hasMessageContaining("Invalid or expired eIDAS state");
  }

  @Test
  void execute_whenProviderMismatch_throwsInvalidState() {
    Instant expiresAt = Instant.now().plusSeconds(600);
    given(eidasStatePort.consume("state-1"))
        .willReturn(Optional.of(new EidasStatePort.EidasStateRecord("state-1", "EIDAS_OTHER", expiresAt)));

    assertThatThrownBy(
        () ->
            sut.execute(
                new HandleEidasCallbackCommand(
                    "EIDAS_EU", "state-1", "auth-code", "external-subject", "high")))
        .isInstanceOf(EidasInvalidStateException.class)
        .hasMessageContaining("provider does not match");
  }
}
