package eu.alboranplus.chinvat.rbac.infrastructure.persistence.mapper;

import eu.alboranplus.chinvat.rbac.domain.model.RoleDefinition;
import eu.alboranplus.chinvat.rbac.infrastructure.persistence.entity.RoleJpaEntity;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RoleJpaMapper {

  public RoleDefinition toDomain(RoleJpaEntity entity) {
    Set<String> permissions =
        Arrays.stream(entity.getPermissionsCsv().split(","))
            .map(String::trim)
            .filter(permission -> !permission.isBlank())
            .collect(Collectors.toUnmodifiableSet());

    return new RoleDefinition(entity.getRoleName(), permissions);
  }

  public RoleJpaEntity toEntity(RoleDefinition roleDefinition) {
    RoleJpaEntity entity = new RoleJpaEntity();
    entity.setRoleName(roleDefinition.roleName());
    entity.setPermissionsCsv(String.join(",", new TreeSet<>(roleDefinition.permissions())));
    return entity;
  }
}
