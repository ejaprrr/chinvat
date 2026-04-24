package eu.alboranplus.chinvat.users.infrastructure.persistence.mapper;

import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserAccountJpaMapper {

  public UserAccountJpaEntity toEntity(UserAccount userAccount) {
    UserAccountJpaEntity entity = new UserAccountJpaEntity();
    entity.setId(userAccount.id());
    entity.setEmail(userAccount.email().value());
    entity.setDisplayName(userAccount.displayName());
    entity.setPasswordHash(userAccount.passwordHash());
    entity.setRolesCsv(String.join(",", new TreeSet<>(userAccount.roles())));
    entity.setActive(userAccount.active());
    entity.setCreatedAt(userAccount.createdAt());
    return entity;
  }

  public UserAccount toDomain(UserAccountJpaEntity entity) {
    Set<String> roles =
        Arrays.stream(entity.getRolesCsv().split(","))
            .map(String::trim)
            .filter(role -> !role.isBlank())
            .collect(Collectors.toUnmodifiableSet());

    return new UserAccount(
        entity.getId(),
        UserEmail.of(entity.getEmail()),
        entity.getDisplayName(),
        entity.getPasswordHash(),
        roles,
        entity.isActive(),
        entity.getCreatedAt());
  }
}
