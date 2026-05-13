package eu.alboranplus.chinvat.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_password")
public class UserPasswordJpaEntity {

  @Id
  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "password_algorithm", nullable = false, length = 20)
  private String passwordAlgorithm;

  @Column(name = "password_changed_at")
  private Instant passwordChangedAt;

  @Column(name = "recovery_required", nullable = false)
  private boolean recoveryRequired;

  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getPasswordAlgorithm() { return passwordAlgorithm; }
  public void setPasswordAlgorithm(String passwordAlgorithm) { this.passwordAlgorithm = passwordAlgorithm; }
  public Instant getPasswordChangedAt() { return passwordChangedAt; }
  public void setPasswordChangedAt(Instant passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }
  public boolean isRecoveryRequired() { return recoveryRequired; }
  public void setRecoveryRequired(boolean recoveryRequired) { this.recoveryRequired = recoveryRequired; }
}
