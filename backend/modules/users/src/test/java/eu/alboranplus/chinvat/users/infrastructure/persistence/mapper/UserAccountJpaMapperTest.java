package eu.alboranplus.chinvat.users.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountJpaMapperTest {

  private final UserAccountJpaMapper mapper = new UserAccountJpaMapper();
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void toEntity_mapsAllFields() {
    UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserAccount domain =
        new UserAccount(
            id1, "alice", "Alice Smith", "+34 600 000 000",
            UserEmail.of("alice@example.com"),
            UserType.INDIVIDUAL, AccessLevel.GOLD,
            "Street 1", "29001", "Malaga", "Spain", "es",
        NOW, NOW, null);

    UserAccountJpaEntity entity = mapper.toEntity(domain);

    assertThat(entity.getId()).isEqualTo(id1);
    assertThat(entity.getUsername()).isEqualTo("alice");
    assertThat(entity.getFullName()).isEqualTo("Alice Smith");
    assertThat(entity.getPhoneNumber()).isEqualTo("+34 600 000 000");
    assertThat(entity.getEmail()).isEqualTo("alice@example.com");
    assertThat(entity.getUserType()).isEqualTo(UserType.INDIVIDUAL);
    assertThat(entity.getAccessLevel()).isEqualTo(AccessLevel.GOLD);
    assertThat(entity.getAddressLine()).isEqualTo("Street 1");
    assertThat(entity.getPostalCode()).isEqualTo("29001");
    assertThat(entity.getCity()).isEqualTo("Malaga");
    assertThat(entity.getCountry()).isEqualTo("Spain");
    assertThat(entity.getDefaultLanguage()).isEqualTo("es");
    assertThat(entity.getCreatedAt()).isEqualTo(NOW);
    assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    assertThat(entity.getDeletedAt()).isNull();
  }

  @Test
  void toDomain_mapsAllFields() {
    UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    UserAccountJpaEntity entity = new UserAccountJpaEntity();
    entity.setId(id2);
    entity.setUsername("bob");
    entity.setFullName("Bob Builder");
    entity.setPhoneNumber(null);
    entity.setEmail("bob@example.com");
    entity.setUserType(UserType.LIBRARY);
    entity.setAccessLevel(AccessLevel.NORMAL);
    entity.setAddressLine(null);
    entity.setPostalCode(null);
    entity.setCity(null);
    entity.setCountry(null);
    entity.setDefaultLanguage("en");
    entity.setCreatedAt(NOW);
    entity.setUpdatedAt(NOW);
    entity.setDeletedAt(null);

    UserAccount domain = mapper.toDomain(entity);

    assertThat(domain.id()).isEqualTo(id2);
    assertThat(domain.username()).isEqualTo("bob");
    assertThat(domain.fullName()).isEqualTo("Bob Builder");
    assertThat(domain.email().value()).isEqualTo("bob@example.com");
    assertThat(domain.userType()).isEqualTo(UserType.LIBRARY);
    assertThat(domain.accessLevel()).isEqualTo(AccessLevel.NORMAL);
    assertThat(domain.defaultLanguage()).isEqualTo("en");
    assertThat(domain.createdAt()).isEqualTo(NOW);
    assertThat(domain.updatedAt()).isEqualTo(NOW);
    assertThat(domain.deletedAt()).isNull();
  }

  @Test
  void roundtrip_domainToEntityToDomain_preservesData() {
    UUID id5 = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UserAccount original =
        new UserAccount(
            id5, "roundtrip", "Roundtrip User", null,
            UserEmail.of("roundtrip@example.com"),
            UserType.INDIVIDUAL, AccessLevel.PREMIUM,
            null, null, null, null, "cs",
        NOW, NOW, null);

    UserAccount roundtripped = mapper.toDomain(mapper.toEntity(original));

    assertThat(roundtripped.id()).isEqualTo(original.id());
    assertThat(roundtripped.email()).isEqualTo(original.email());
    assertThat(roundtripped.username()).isEqualTo(original.username());
    assertThat(roundtripped.accessLevel()).isEqualTo(original.accessLevel());
    assertThat(roundtripped.createdAt()).isEqualTo(original.createdAt());
  }
}
