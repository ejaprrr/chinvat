package eu.alboranplus.chinvat.common.audit;

import java.util.Map;

public interface AuditFacade {
  void log(String eventType, String actor, Long actorUserId, Map<String, Object> details);
}