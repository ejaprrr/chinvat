package eu.alboranplus.chinvat.trust.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "certificate_credential")
public class CertificateCredentialJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(length = 80)
  private String providerCode;

  @Column(nullable = false, length = 80)
  private String credentialType;

  @Column(nullable = false, length = 80)
  private String trustStatus;

  @Column(nullable = false, length = 80)
  private String revocationStatus;

  @Column(length = 80)
  private String assuranceLevel;

  @Column(nullable = false, length = 80)
  private String registrationSource;

  @Column(nullable = false, length = 64, unique = true)
  private String thumbprintSha256;

  @Column(nullable = false, length = 1024)
  private String subjectDn;

  @Column(nullable = false, length = 1024)
  private String issuerDn;

  @Column(nullable = false, length = 128)
  private String serialNumber;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String certificatePem;

  @Column(nullable = false)
  private Instant notBefore;

  @Column(nullable = false)
  private Instant notAfter;

  @Column(length = 120)
  private String approvedBy;

  private Instant approvedAt;

  @Column(length = 120)
  private String revokedBy;

  private Instant revokedAt;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

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

  public String getProviderCode() {
    return providerCode;
  }

  public void setProviderCode(String providerCode) {
    this.providerCode = providerCode;
  }

  public String getCredentialType() {
    return credentialType;
  }

  public void setCredentialType(String credentialType) {
    this.credentialType = credentialType;
  }

  public String getTrustStatus() {
    return trustStatus;
  }

  public void setTrustStatus(String trustStatus) {
    this.trustStatus = trustStatus;
  }

  public String getRevocationStatus() {
    return revocationStatus;
  }

  public void setRevocationStatus(String revocationStatus) {
    this.revocationStatus = revocationStatus;
  }

  public String getAssuranceLevel() {
    return assuranceLevel;
  }

  public void setAssuranceLevel(String assuranceLevel) {
    this.assuranceLevel = assuranceLevel;
  }

  public String getRegistrationSource() {
    return registrationSource;
  }

  public void setRegistrationSource(String registrationSource) {
    this.registrationSource = registrationSource;
  }

  public String getThumbprintSha256() {
    return thumbprintSha256;
  }

  public void setThumbprintSha256(String thumbprintSha256) {
    this.thumbprintSha256 = thumbprintSha256;
  }

  public String getSubjectDn() {
    return subjectDn;
  }

  public void setSubjectDn(String subjectDn) {
    this.subjectDn = subjectDn;
  }

  public String getIssuerDn() {
    return issuerDn;
  }

  public void setIssuerDn(String issuerDn) {
    this.issuerDn = issuerDn;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getCertificatePem() {
    return certificatePem;
  }

  public void setCertificatePem(String certificatePem) {
    this.certificatePem = certificatePem;
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

  public String getApprovedBy() {
    return approvedBy;
  }

  public void setApprovedBy(String approvedBy) {
    this.approvedBy = approvedBy;
  }

  public Instant getApprovedAt() {
    return approvedAt;
  }

  public void setApprovedAt(Instant approvedAt) {
    this.approvedAt = approvedAt;
  }

  public String getRevokedBy() {
    return revokedBy;
  }

  public void setRevokedBy(String revokedBy) {
    this.revokedBy = revokedBy;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(Instant revokedAt) {
    this.revokedAt = revokedAt;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
