package eu.alboranplus.chinvat.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_certificate")
public class UserCertificateJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "thumbprint_sha256", nullable = false, unique = true, length = 64)
  private String thumbprintSha256;

  @Column(name = "not_before", nullable = false)
  private Instant notBefore;

  @Column(name = "not_after", nullable = false)
  private Instant notAfter;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getThumbprintSha256() {
    return thumbprintSha256;
  }

  public void setThumbprintSha256(String thumbprintSha256) {
    this.thumbprintSha256 = thumbprintSha256;
  }

  public Instant getNotBefore() {
    return notBefore;
  }

  public void setNotBefore(Instant notBefore) {
    this.notBefore = notBefore;
  }

  public Instant getNotAfter() {
    return notAfter;
  }

  public void setNotAfter(Instant notAfter) {
    this.notAfter = notAfter;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }
}