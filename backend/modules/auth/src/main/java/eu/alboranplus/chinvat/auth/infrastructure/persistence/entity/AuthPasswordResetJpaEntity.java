package eu.alboranplus.chinvat.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_password_reset")
public class AuthPasswordResetJpaEntity {

  @Id
  @Column(columnDefinition = "uuid", updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "reset_token_hash", nullable = false, length = 255)
  private String resetTokenHash;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  @Column(name = "client_ip", length = 64)
  private String clientIp;

  @Column(name = "user_agent", length = 512)
  private String userAgent;

  public AuthPasswordResetJpaEntity() {}

  public AuthPasswordResetJpaEntity(
      UUID id,
      Long userId,
      String resetTokenHash,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent) {
    this.id = id;
    this.userId = userId;
    this.resetTokenHash = resetTokenHash;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.clientIp = clientIp;
    this.userAgent = userAgent;
  }

  public UUID getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getResetTokenHash() {
    return resetTokenHash;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public void setConsumedAt(Instant consumedAt) {
    this.consumedAt = consumedAt;
  }
}

