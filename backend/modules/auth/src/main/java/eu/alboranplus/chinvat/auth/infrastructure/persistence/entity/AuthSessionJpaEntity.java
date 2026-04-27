package eu.alboranplus.chinvat.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
public class AuthSessionJpaEntity {

  @Id
  @Column(columnDefinition = "uuid", updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "session_token_hash", nullable = false, unique = true, length = 255)
  private String sessionTokenHash;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "client_ip", length = 64)
  private String clientIp;

  @Column(name = "user_agent", length = 512)
  private String userAgent;

  public AuthSessionJpaEntity() {}

  public AuthSessionJpaEntity(
      UUID id,
      Long userId,
      String sessionTokenHash,
      Instant issuedAt,
      Instant expiresAt,
      String clientIp,
      String userAgent) {
    this.id = id;
    this.userId = userId;
    this.sessionTokenHash = sessionTokenHash;
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

  public String getSessionTokenHash() {
    return sessionTokenHash;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public String getClientIp() {
    return clientIp;
  }

  public String getUserAgent() {
    return userAgent;
  }
}
