package eu.alboranplus.chinvat.rbac.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.rbac.application.port.out.RbacRepositoryPort;
import eu.alboranplus.chinvat.rbac.domain.model.PermissionDefinition;
import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.jpa.RoleJpaRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RbacRepositoryAdapter implements RbacRepositoryPort {

  private final RoleJpaRepository roleJpaRepository;
  private final JdbcTemplate jdbcTemplate;

  public RbacRepositoryAdapter(
      RoleJpaRepository roleJpaRepository, JdbcTemplate jdbcTemplate) {
    this.roleJpaRepository = roleJpaRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<RoleDefinition> findByRoleName(String roleName) {
    return roleJpaRepository.findByRoleNameIgnoreCase(roleName)
        .map(entity -> new RoleDefinition(entity.getRoleName(), Set.of()))
        .map(this::mergeWithNormalizedPermissions);
  }

  @Override
  public Set<RoleDefinition> findByRoleNames(Set<String> roleNames) {
    Map<String, Set<String>> normalizedPermissionsByRole =
        findNormalizedPermissionsByRoleNames(roleNames);

    return roleJpaRepository.findByRoleNameIn(roleNames).stream()
        .map(entity -> new RoleDefinition(entity.getRoleName(), Set.of()))
        .map(
            role -> {
              Set<String> normalized = normalizedPermissionsByRole.get(role.roleName());
              if (normalized == null || normalized.isEmpty()) {
                return role;
              }
              return new RoleDefinition(role.roleName(), normalized);
            })
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public List<PermissionDefinition> findAllPermissions() {
    return jdbcTemplate.query(
        """
        SELECT permission_code, description
        FROM rbac_permission
        ORDER BY permission_code
        """,
        permissionRowMapper());
  }

  @Override
  public Optional<PermissionDefinition> findPermissionByCode(String permissionCode) {
    List<PermissionDefinition> rows =
        jdbcTemplate.query(
            """
            SELECT permission_code, description
            FROM rbac_permission
            WHERE UPPER(permission_code) = UPPER(?)
            """,
            permissionRowMapper(),
            permissionCode);
    return rows.stream().findFirst();
  }

  @Override
  public PermissionDefinition createPermission(PermissionDefinition permissionDefinition) {
    jdbcTemplate.update(
        """
        INSERT INTO rbac_permission (permission_code, description, created_at)
        VALUES (?, ?, ?)
        """,
        permissionDefinition.permissionCode(),
        permissionDefinition.description(),
        Timestamp.from(Instant.now()));
    return permissionDefinition;
  }

  @Override
  public PermissionDefinition updatePermission(PermissionDefinition permissionDefinition) {
    jdbcTemplate.update(
        """
        UPDATE rbac_permission
        SET description = ?
        WHERE UPPER(permission_code) = UPPER(?)
        """,
        permissionDefinition.description(),
        permissionDefinition.permissionCode());
    return permissionDefinition;
  }

  @Override
  public void deletePermissionByCode(String permissionCode) {
    jdbcTemplate.update(
        """
        DELETE FROM rbac_permission
        WHERE UPPER(permission_code) = UPPER(?)
        """,
        permissionCode);
  }

  @Override
  public boolean roleExists(String roleName) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rbac_role WHERE UPPER(role_name) = UPPER(?)",
            Integer.class,
            roleName);
    return count != null && count > 0;
  }

  @Override
  public boolean userExists(Long userId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM \"user\" WHERE id = ?",
            Integer.class,
            userId);
    return count != null && count > 0;
  }

  @Override
  public void assignRoleToUser(Long userId, String roleName, String assignedBy) {
    jdbcTemplate.update(
        """
        INSERT INTO user_role (user_id, role_id, assigned_at, assigned_by)
        SELECT ?, r.id, ?, ?
        FROM rbac_role r
        WHERE UPPER(r.role_name) = UPPER(?)
        ON CONFLICT (user_id, role_id) DO NOTHING
        """,
        userId,
        Timestamp.from(Instant.now()),
        assignedBy,
        roleName);
  }

  @Override
  public void removeRoleFromUser(Long userId, String roleName) {
    jdbcTemplate.update(
        """
        DELETE FROM user_role ur
        USING rbac_role r
        WHERE ur.role_id = r.id
          AND ur.user_id = ?
          AND UPPER(r.role_name) = UPPER(?)
        """,
        userId,
        roleName);
  }

  @Override
  public Set<String> findRoleNamesByUserId(Long userId) {
    return Set.copyOf(
        jdbcTemplate.queryForList(
            """
            SELECT r.role_name
            FROM user_role ur
            JOIN rbac_role r ON r.id = ur.role_id
            WHERE ur.user_id = ?
            """,
            String.class,
            userId));
  }

  private RoleDefinition mergeWithNormalizedPermissions(RoleDefinition csvRoleDefinition) {
    Set<String> normalizedPermissions =
        findNormalizedPermissionsByRoleNames(Set.of(csvRoleDefinition.roleName()))
            .getOrDefault(csvRoleDefinition.roleName(), Set.of());

    if (normalizedPermissions.isEmpty()) {
      return csvRoleDefinition;
    }

    return new RoleDefinition(csvRoleDefinition.roleName(), normalizedPermissions);
  }

  private Map<String, Set<String>> findNormalizedPermissionsByRoleNames(Set<String> roleNames) {
    if (roleNames.isEmpty()) {
      return Map.of();
    }

    String placeholders = roleNames.stream().map(ignored -> "?").collect(Collectors.joining(","));
    List<Object> args =
      roleNames.stream().map(role -> (Object) role.toUpperCase(Locale.ROOT)).toList();

    List<Map<String, Object>> rows =
        jdbcTemplate.queryForList(
            """
            SELECT UPPER(r.role_name) AS role_name, p.permission_code
            FROM rbac_role r
            JOIN rbac_role_permission rp ON rp.role_id = r.id
            JOIN rbac_permission p ON p.id = rp.permission_id
            WHERE UPPER(r.role_name) IN (
            """
                + placeholders
                + ")",
            args.toArray());

    Map<String, Set<String>> permissionsByRole = new HashMap<>();
    for (Map<String, Object> row : rows) {
      String roleName = (String) row.get("role_name");
      String permissionCode = (String) row.get("permission_code");
      permissionsByRole.computeIfAbsent(roleName, ignored -> new java.util.HashSet<>()).add(permissionCode);
    }

    return permissionsByRole.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
  }

  private RowMapper<PermissionDefinition> permissionRowMapper() {
    return (ResultSet rs, int rowNum) -> mapPermission(rs);
  }

  private PermissionDefinition mapPermission(ResultSet rs) throws SQLException {
    return new PermissionDefinition(rs.getString("permission_code"), rs.getString("description"));
  }
}
