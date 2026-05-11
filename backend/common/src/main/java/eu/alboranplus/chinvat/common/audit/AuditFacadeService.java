package eu.alboranplus.chinvat.common.audit;

import eu.alboranplus.chinvat.common.audit.persistence.AuditEventJpaEntity;
import eu.alboranplus.chinvat.common.audit.persistence.AuditEventJpaRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditFacadeService implements AuditFacade {

  private final AuditEventJpaRepository auditEventJpaRepository;

  public AuditFacadeService(AuditEventJpaRepository auditEventJpaRepository) {
    this.auditEventJpaRepository = auditEventJpaRepository;
  }

  @Override
  public void log(String eventType, String actor, UUID actorUserId, Map<String, Object> details) {
    Map<String, Object> payload = new LinkedHashMap<>(details);
    if (actor != null) {
      payload.put("actor", actor);
    }

    auditEventJpaRepository.save(
        new AuditEventJpaEntity(eventType, actorUserId, Map.copyOf(payload), Instant.now()));
  }
}