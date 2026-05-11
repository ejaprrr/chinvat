package eu.alboranplus.chinvat.users.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import eu.alboranplus.chinvat.users.infrastructure.persistence.jpa.UserAccountJpaRepository;
import eu.alboranplus.chinvat.users.infrastructure.persistence.jpa.UserCertificateJpaRepository;
import eu.alboranplus.chinvat.users.infrastructure.persistence.mapper.UserAccountJpaMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class UsersRepositoryAdapter implements UsersRepositoryPort {

  private final UserAccountJpaRepository userAccountJpaRepository;
  private final UserCertificateJpaRepository userCertificateJpaRepository;
  private final UserAccountJpaMapper userAccountJpaMapper;

  public UsersRepositoryAdapter(
      UserAccountJpaRepository userAccountJpaRepository,
      UserCertificateJpaRepository userCertificateJpaRepository,
      UserAccountJpaMapper userAccountJpaMapper) {
    this.userAccountJpaRepository = userAccountJpaRepository;
    this.userCertificateJpaRepository = userCertificateJpaRepository;
    this.userAccountJpaMapper = userAccountJpaMapper;
  }

  @Override
  public boolean existsByEmail(UserEmail email) {
    return userAccountJpaRepository.existsByEmailIgnoreCase(email.value());
  }

  @Override
  public boolean existsByUsername(String username) {
    return userAccountJpaRepository.existsByUsernameIgnoreCase(username);
  }

  @Override
  public Optional<UserAccount> findByEmail(UserEmail email) {
    return userAccountJpaRepository
        .findByEmailIgnoreCase(email.value())
        .map(userAccountJpaMapper::toDomain);
  }

  @Override
  public Optional<UserAccount> findById(UUID id) {
    return userAccountJpaRepository.findById(id).map(userAccountJpaMapper::toDomain);
  }

  @Override
  public Optional<UserAccount> findByCertificateThumbprint(String thumbprintSha256, Instant now) {
    return userCertificateJpaRepository
        .findActiveUserIdByThumbprintSha256(thumbprintSha256, now)
        .flatMap(userAccountJpaRepository::findById)
        .map(userAccountJpaMapper::toDomain);
  }

  @Override
  public List<UserAccount> findAll() {
    return userAccountJpaRepository.findAll().stream()
        .map(userAccountJpaMapper::toDomain)
        .toList();
  }

  @Override
  public Page<UserAccount> findAll(Pageable pageable) {
    return userAccountJpaRepository.findAll(pageable).map(userAccountJpaMapper::toDomain);
  }

  @Override
  public UserAccount save(UserAccount userAccount) {
    UserAccountJpaEntity persisted =
        userAccountJpaRepository.save(userAccountJpaMapper.toEntity(userAccount));
    return userAccountJpaMapper.toDomain(persisted);
  }

  @Override
  public void deleteById(UUID id) {
    userAccountJpaRepository.deleteById(id);
  }
}

