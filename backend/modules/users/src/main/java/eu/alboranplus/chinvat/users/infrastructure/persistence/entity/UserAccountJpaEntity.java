package eu.alboranplus.chinvat.users.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "\"user\"")
public class UserAccountJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "phone_number", length = 40)
  private String phoneNumber;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false, length = 20)
  private eu.alboranplus.chinvat.users.domain.model.UserType userType;

  @Enumerated(EnumType.STRING)
  @Column(name = "access_level", nullable = false, length = 20)
  private eu.alboranplus.chinvat.users.domain.model.AccessLevel accessLevel;

  @Column(name = "address_line", length = 255)
  private String addressLine;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String country;

  @Column(name = "default_language", length = 12)
  private String defaultLanguage;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public eu.alboranplus.chinvat.users.domain.model.UserType getUserType() { return userType; }
  public void setUserType(eu.alboranplus.chinvat.users.domain.model.UserType userType) { this.userType = userType; }
  public eu.alboranplus.chinvat.users.domain.model.AccessLevel getAccessLevel() { return accessLevel; }
  public void setAccessLevel(eu.alboranplus.chinvat.users.domain.model.AccessLevel accessLevel) { this.accessLevel = accessLevel; }
  public String getAddressLine() { return addressLine; }
  public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
  public String getPostalCode() { return postalCode; }
  public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public String getCountry() { return country; }
  public void setCountry(String country) { this.country = country; }
  public String getDefaultLanguage() { return defaultLanguage; }
  public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
