package eu.alboranplus.chinvat.rbac.application.usecase;

import java.util.Map;
import java.util.Set;

public final class BuiltinRolePermissions {

  private static final Map<String, Set<String>> BUILTIN =
      Map.of(
      "USER", Set.of("PROFILE:READ", "PROFILE:WRITE"),
          "ADMIN", Set.of("PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE"),
          "SUPERADMIN",
              Set.of(
                  "PROFILE:READ", "PROFILE:WRITE", "USERS:MANAGE", "RBAC:MANAGE", "AUTH:MANAGE"));

  private BuiltinRolePermissions() {}

  public static Set<String> permissionsFor(String roleName) {
    return BUILTIN.getOrDefault(roleName.toUpperCase(), Set.of());
  }
}
