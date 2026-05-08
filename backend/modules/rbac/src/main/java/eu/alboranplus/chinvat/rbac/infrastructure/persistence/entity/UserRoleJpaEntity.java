package eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_role")
@IdClass(UserRoleId.class)
public class UserRoleJpaEntity {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "assigned_by", length = 120)
  private String assignedBy;

  public UserRoleJpaEntity() {}

  public UserRoleJpaEntity(Long userId, Long roleId, Instant assignedAt, String assignedBy) {
    this.userId = userId;
    this.roleId = roleId;
    this.assignedAt = assignedAt;
    this.assignedBy = assignedBy;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(Instant assignedAt) {
    this.assignedAt = assignedAt;
  }

  public String getAssignedBy() {
    return assignedBy;
  }

  public void setAssignedBy(String assignedBy) {
    this.assignedBy = assignedBy;
  }
}

/**
 * Composite key class for UserRoleJpaEntity. Required for @IdClass annotation.
 */
class UserRoleId implements Serializable {
  private Long userId;
  private Long roleId;

  public UserRoleId() {}

  public UserRoleId(Long userId, Long roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserRoleId that = (UserRoleId) o;

    if (!userId.equals(that.userId)) return false;
    return roleId.equals(that.roleId);
  }

  @Override
  public int hashCode() {
    int result = userId.hashCode();
    result = 31 * result + roleId.hashCode();
    return result;
  }
}
