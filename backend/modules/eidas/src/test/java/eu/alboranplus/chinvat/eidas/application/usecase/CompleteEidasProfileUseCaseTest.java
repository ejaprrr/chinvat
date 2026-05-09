package eu.alboranplus.chinvat.eidas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import eu.alboranplus.chinvat.eidas.application.command.CompleteEidasProfileCommand;
import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import eu.alboranplus.chinvat.eidas.application.port.out.ExternalIdentityLifecyclePort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasExternalIdentityNotFoundException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProfileCompletionException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompleteEidasProfileUseCaseTest {

  @Mock private ExternalIdentityLifecyclePort externalIdentityLifecyclePort;

  private CompleteEidasProfileUseCase sut;

  @BeforeEach
  void setUp() {
    sut = new CompleteEidasProfileUseCase(externalIdentityLifecyclePort);
  }

  @Test
  void execute_whenPendingIdentityExists_linksUserAndActivates() {
    Instant createdAt = Instant.parse("2026-05-09T09:55:00Z");
    ExternalIdentityView pending =
        new ExternalIdentityView(
            10L,
            null,
            "EIDAS_EU",
            "EIDAS_BROKER",
            "subject-1",
            "high",
            "pid",
            null,
            null,
            null,
            "Maria",
            "Example",
            "1990-01-01",
            "{}",
            "PENDING_PROFILE",
            null,
            null,
            null,
            null,
            null,
            createdAt,
            createdAt);

    given(externalIdentityLifecyclePort.findLatest("EIDAS_EU", "subject-1"))
        .willReturn(Optional.of(pending));
    given(externalIdentityLifecyclePort.save(any(ExternalIdentityView.class)))
        .willAnswer(invocation -> invocation.getArgument(0, ExternalIdentityView.class));

    var result =
        sut.execute(new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", 77L, "ES/123", "ES"));

    assertThat(result.userId()).isEqualTo(77L);
    assertThat(result.currentStatus()).isEqualTo("ACTIVE");
    assertThat(result.linkedAt()).isNotNull();
  }

  @Test
  void execute_whenMissingIdentity_throwsNotFound() {
    given(externalIdentityLifecyclePort.findLatest("EIDAS_EU", "subject-1"))
        .willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                sut.execute(
                    new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", 77L, null, null)))
        .isInstanceOf(EidasExternalIdentityNotFoundException.class);
  }

  @Test
  void execute_whenIdentityLinkedToDifferentUser_throwsConflict() {
    Instant now = Instant.parse("2026-05-09T10:00:00Z");
    ExternalIdentityView linked =
        new ExternalIdentityView(
            11L,
            88L,
            "EIDAS_EU",
            "EIDAS_BROKER",
            "subject-1",
            "high",
            "pid",
            null,
            null,
            null,
            "Maria",
            "Example",
            "1990-01-01",
            "{}",
            "ACTIVE",
            null,
            null,
            null,
            now,
            null,
            now,
            now);

    given(externalIdentityLifecyclePort.findLatest("EIDAS_EU", "subject-1"))
        .willReturn(Optional.of(linked));

    assertThatThrownBy(
            () ->
                sut.execute(
                    new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", 77L, null, null)))
        .isInstanceOf(EidasProfileCompletionException.class)
        .hasMessageContaining("already linked");
  }
}
