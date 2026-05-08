package eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "rbac_role_permission")
@IdClass(RbacRolePermissionId.class)
public class RbacRolePermissionJpaEntity {

  @Id
  @Column(name = "role_id")
  private Long roleId;

  @Id
  @Column(name = "permission_id")
  private Long permissionId;

  public RbacRolePermissionJpaEntity() {}

  public RbacRolePermissionJpaEntity(Long roleId, Long permissionId) {
    this.roleId = roleId;
    this.permissionId = permissionId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public Long getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(Long permissionId) {
    this.permissionId = permissionId;
  }
}

/**
 * Composite key class for RbacRolePermissionJpaEntity. Required for @IdClass annotation.
 */
class RbacRolePermissionId implements Serializable {
  private Long roleId;
  private Long permissionId;

  public RbacRolePermissionId() {}

  public RbacRolePermissionId(Long roleId, Long permissionId) {
    this.roleId = roleId;
    this.permissionId = permissionId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public Long getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(Long permissionId) {
    this.permissionId = permissionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RbacRolePermissionId that = (RbacRolePermissionId) o;

    if (!roleId.equals(that.roleId)) return false;
    return permissionId.equals(that.permissionId);
  }

  @Override
  public int hashCode() {
    int result = roleId.hashCode();
    result = 31 * result + permissionId.hashCode();
    return result;
  }
}
