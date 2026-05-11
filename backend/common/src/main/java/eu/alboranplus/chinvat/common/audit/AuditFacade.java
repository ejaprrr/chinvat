package eu.alboranplus.chinvat.common.audit;

import java.util.Map;
import java.util.UUID;

public interface AuditFacade {
  void log(String eventType, String actor, UUID actorUserId, Map<String, Object> details);
}