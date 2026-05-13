package eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "rbac_role_permission")
@IdClass(RbacRolePermissionId.class)
public class RbacRolePermissionJpaEntity {

  @Id
  @Column(name = "role_id", columnDefinition = "uuid")
  private UUID roleId;

  @Id
  @Column(name = "permission_id", columnDefinition = "uuid")
  private UUID permissionId;

  public RbacRolePermissionJpaEntity() {}

  public RbacRolePermissionJpaEntity(UUID roleId, UUID permissionId) {
    this.roleId = roleId;
    this.permissionId = permissionId;
  }

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public UUID getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(UUID permissionId) {
    this.permissionId = permissionId;
  }
}

/**
 * Composite key class for RbacRolePermissionJpaEntity. Required for @IdClass annotation.
 */
class RbacRolePermissionId implements Serializable {
  private UUID roleId;
  private UUID permissionId;

  public RbacRolePermissionId() {}

  public RbacRolePermissionId(UUID roleId, UUID permissionId) {
    this.roleId = roleId;
    this.permissionId = permissionId;
  }

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public UUID getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(UUID permissionId) {
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
