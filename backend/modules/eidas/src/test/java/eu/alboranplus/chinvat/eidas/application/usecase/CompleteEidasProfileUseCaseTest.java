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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompleteEidasProfileUseCaseTest {

    private static final UUID UUID_10 = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID UUID_11 = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID UUID_77 = UUID.fromString("00000000-0000-0000-0000-00000000004d");
    private static final UUID UUID_88 = UUID.fromString("00000000-0000-0000-0000-000000000058");

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
                        UUID_10,
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
        sut.execute(
            new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", UUID_77, "ES/123", "ES"));

    assertThat(result.userId()).isEqualTo(UUID_77);
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
                    new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", UUID_77, null, null)))
        .isInstanceOf(EidasExternalIdentityNotFoundException.class);
  }

  @Test
  void execute_whenIdentityLinkedToDifferentUser_throwsConflict() {
    Instant now = Instant.parse("2026-05-09T10:00:00Z");
    ExternalIdentityView linked =
        new ExternalIdentityView(
                        UUID_11,
                        UUID_88,
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
                                        new CompleteEidasProfileCommand("EIDAS_EU", "subject-1", UUID_77, null, null)))
        .isInstanceOf(EidasProfileCompletionException.class)
        .hasMessageContaining("already linked");
  }
}
