package eu.alboranplus.chinvat.users.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.users.application.port.out.UsersPasswordPort;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserPasswordJpaEntity;
import eu.alboranplus.chinvat.users.infrastructure.persistence.jpa.UserPasswordJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UsersPasswordAdapter implements UsersPasswordPort {

  private final UserPasswordJpaRepository userPasswordJpaRepository;

  public UsersPasswordAdapter(UserPasswordJpaRepository userPasswordJpaRepository) {
    this.userPasswordJpaRepository = userPasswordJpaRepository;
  }

  @Override
  public Optional<String> findHashByUserId(UUID userId) {
    return userPasswordJpaRepository.findById(userId).map(UserPasswordJpaEntity::getPasswordHash);
  }

  @Override
  public void save(UUID userId, String passwordHash, String algorithm) {
    UserPasswordJpaEntity entity =
        userPasswordJpaRepository.findById(userId).orElse(new UserPasswordJpaEntity());
    entity.setUserId(userId);
    entity.setPasswordHash(passwordHash);
    entity.setPasswordAlgorithm(algorithm);
    entity.setPasswordChangedAt(Instant.now());
    entity.setRecoveryRequired(false);
    userPasswordJpaRepository.save(entity);
  }
}

