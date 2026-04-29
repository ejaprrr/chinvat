package eu.alboranplus.chinvat.users.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.users.application.port.out.UsersRoleRepositoryPort;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsersRoleRepositoryAdapter implements UsersRoleRepositoryPort {

  private final JdbcTemplate jdbcTemplate;

  public UsersRoleRepositoryAdapter(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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
}
