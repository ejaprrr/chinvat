package eu.alboranplus.chinvat.eidas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "external_identity")
public class ExternalIdentityJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "provider_id", columnDefinition = "uuid")
  private UUID providerId;

  @Column(nullable = false, length = 80)
  private String providerCode;

  @Column(nullable = false, length = 80)
  private String identitySource;

  @Column(nullable = false, length = 255)
  private String externalSubjectId;

  @Column(length = 80)
  private String assuranceLevel;

  @Column(length = 255)
  private String personIdentifier;

  @Column(length = 255)
  private String legalPersonIdentifier;

  @Column(length = 255)
  private String identityReference;

  @Column(length = 80)
  private String nationality;

  @Column(length = 160)
  private String firstName;

  @Column(length = 160)
  private String familyName;

  @Column(length = 40)
  private String dateOfBirth;

  @Column(columnDefinition = "TEXT")
  private String rawClaimsJson;

  @Column(nullable = false, length = 80)
  private String currentStatus;

  @Column(length = 120)
  private String reviewedBy;

  private Instant reviewedAt;

  @Column(columnDefinition = "TEXT")
  private String reviewReason;

  private Instant linkedAt;

  private Instant unlinkedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getProviderId() {
    return providerId;
  }

  public void setProviderId(UUID providerId) {
    this.providerId = providerId;
  }

  public String getProviderCode() {
    return providerCode;
  }

  public void setProviderCode(String providerCode) {
    this.providerCode = providerCode;
  }

  public String getIdentitySource() {
    return identitySource;
  }

  public void setIdentitySource(String identitySource) {
    this.identitySource = identitySource;
  }

  public String getExternalSubjectId() {
    return externalSubjectId;
  }

  public void setExternalSubjectId(String externalSubjectId) {
    this.externalSubjectId = externalSubjectId;
  }

  public String getAssuranceLevel() {
    return assuranceLevel;
  }

  public void setAssuranceLevel(String assuranceLevel) {
    this.assuranceLevel = assuranceLevel;
  }

  public String getPersonIdentifier() {
    return personIdentifier;
  }

  public void setPersonIdentifier(String personIdentifier) {
    this.personIdentifier = personIdentifier;
  }

  public String getLegalPersonIdentifier() {
    return legalPersonIdentifier;
  }

  public void setLegalPersonIdentifier(String legalPersonIdentifier) {
    this.legalPersonIdentifier = legalPersonIdentifier;
  }

  public String getIdentityReference() {
    return identityReference;
  }

  public void setIdentityReference(String identityReference) {
    this.identityReference = identityReference;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public void setFamilyName(String familyName) {
    this.familyName = familyName;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getRawClaimsJson() {
    return rawClaimsJson;
  }

  public void setRawClaimsJson(String rawClaimsJson) {
    this.rawClaimsJson = rawClaimsJson;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }

  public void setCurrentStatus(String currentStatus) {
    this.currentStatus = currentStatus;
  }

  public String getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(String reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public void setReviewedAt(Instant reviewedAt) {
    this.reviewedAt = reviewedAt;
  }

  public String getReviewReason() {
    return reviewReason;
  }

  public void setReviewReason(String reviewReason) {
    this.reviewReason = reviewReason;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }

  public void setLinkedAt(Instant linkedAt) {
    this.linkedAt = linkedAt;
  }

  public Instant getUnlinkedAt() {
    return unlinkedAt;
  }

  public void setUnlinkedAt(Instant unlinkedAt) {
    this.unlinkedAt = unlinkedAt;
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