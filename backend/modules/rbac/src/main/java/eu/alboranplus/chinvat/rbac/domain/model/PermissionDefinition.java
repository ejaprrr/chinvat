package eu.alboranplus.chinvat.rbac.domain.model;

public record PermissionDefinition(String permissionCode, String description) {

  public PermissionDefinition {
    if (permissionCode == null || permissionCode.isBlank()) {
      throw new IllegalArgumentException("Permission code must not be blank");
    }

    permissionCode = permissionCode.trim().toUpperCase();
    description = description == null ? null : description.trim();
  }
}
