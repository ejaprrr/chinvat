package eu.alboranplus.chinvat.users.infrastructure.persistence.mapper;

import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAccountJpaMapper {

  public UserAccountJpaEntity toEntity(UserAccount userAccount) {
    UserAccountJpaEntity entity = new UserAccountJpaEntity();
    entity.setId(userAccount.id());
    entity.setUsername(userAccount.username());
    entity.setFullName(userAccount.fullName());
    entity.setPhoneNumber(userAccount.phoneNumber());
    entity.setEmail(userAccount.email().value());
    entity.setUserType(userAccount.userType());
    entity.setAccessLevel(userAccount.accessLevel());
    entity.setAddressLine(userAccount.addressLine());
    entity.setPostalCode(userAccount.postalCode());
    entity.setCity(userAccount.city());
    entity.setCountry(userAccount.country());
    entity.setDefaultLanguage(userAccount.defaultLanguage());
    entity.setCreatedAt(userAccount.createdAt());
    entity.setUpdatedAt(userAccount.updatedAt());
    return entity;
  }

  public UserAccount toDomain(UserAccountJpaEntity entity) {
    return new UserAccount(
        entity.getId(),
        entity.getUsername(),
        entity.getFullName(),
        entity.getPhoneNumber(),
        UserEmail.of(entity.getEmail()),
        entity.getUserType(),
        entity.getAccessLevel(),
        entity.getAddressLine(),
        entity.getPostalCode(),
        entity.getCity(),
        entity.getCountry(),
        entity.getDefaultLanguage(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}

