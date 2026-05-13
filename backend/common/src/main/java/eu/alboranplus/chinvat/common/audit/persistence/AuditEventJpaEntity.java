package eu.alboranplus.chinvat.common.audit.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auth_audit_event")
public class AuditEventJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "event_type", nullable = false, length = 120)
  private String eventType;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "details", nullable = false)
  private Map<String, Object> details;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public AuditEventJpaEntity() {}

  public AuditEventJpaEntity(
      String eventType, UUID userId, Map<String, Object> details, Instant createdAt) {
    this.eventType = eventType;
    this.userId = userId;
    this.details = details;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public UUID getUserId() {
    return userId;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}