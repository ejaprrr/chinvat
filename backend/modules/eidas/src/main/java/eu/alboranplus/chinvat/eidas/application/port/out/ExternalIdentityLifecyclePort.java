package eu.alboranplus.chinvat.eidas.application.port.out;

import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import java.util.Optional;
import java.util.UUID;

public interface ExternalIdentityLifecyclePort {
  Optional<UUID> findLinkedUserId(String providerCode, String externalSubjectId);

  Optional<ExternalIdentityView> findLatest(String providerCode, String externalSubjectId);

  ExternalIdentityView save(ExternalIdentityView identity);
}