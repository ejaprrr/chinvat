package eu.alboranplus.chinvat.eidas.application.port.out;

import eu.alboranplus.chinvat.eidas.application.dto.ExternalIdentityView;
import java.util.Optional;

public interface ExternalIdentityLifecyclePort {
  Optional<Long> findLinkedUserId(String providerCode, String externalSubjectId);

  Optional<ExternalIdentityView> findLatest(String providerCode, String externalSubjectId);

  ExternalIdentityView save(ExternalIdentityView identity);
}