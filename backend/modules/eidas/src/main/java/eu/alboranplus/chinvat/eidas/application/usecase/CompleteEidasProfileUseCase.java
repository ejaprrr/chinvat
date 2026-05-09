package eu.alboranplus.chinvat.eidas.application.usecase;

import eu.alboranplus.chinvat.eidas.application.command.CompleteEidasProfileCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProfileCompletionView;
import eu.alboranplus.chinvat.eidas.application.port.out.ExternalIdentityLifecyclePort;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasExternalIdentityNotFoundException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProfileCompletionException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteEidasProfileUseCase {

  private final ExternalIdentityLifecyclePort externalIdentityLifecyclePort;

  public CompleteEidasProfileUseCase(ExternalIdentityLifecyclePort externalIdentityLifecyclePort) {
    this.externalIdentityLifecyclePort = externalIdentityLifecyclePort;
  }

  @Transactional
  public EidasProfileCompletionView execute(CompleteEidasProfileCommand command) {
    var existing =
        externalIdentityLifecyclePort
            .findLatest(command.providerCode(), command.externalSubjectId())
            .orElseThrow(
                () ->
                    new EidasExternalIdentityNotFoundException(
                        "External identity not found for provider="
                            + command.providerCode()
                            + " and subject="
                            + command.externalSubjectId()));

    if (existing.userId() != null && !existing.userId().equals(command.userId())) {
      throw new EidasProfileCompletionException(
          "External identity already linked to a different user");
    }

    if (!"PENDING_PROFILE".equals(existing.currentStatus()) && existing.userId() == null) {
      throw new EidasProfileCompletionException(
          "External identity is not eligible for profile completion in current status: "
              + existing.currentStatus());
    }

    Instant now = Instant.now();
    var updated =
        externalIdentityLifecyclePort.save(
            existing.withLinkedUser(
                command.userId(),
                "ACTIVE",
                command.identityReference(),
                command.nationality(),
                now));

    return new EidasProfileCompletionView(
        updated.providerCode(),
        updated.externalSubjectId(),
        updated.userId(),
        updated.currentStatus(),
        updated.linkedAt(),
        updated.updatedAt());
  }
}
