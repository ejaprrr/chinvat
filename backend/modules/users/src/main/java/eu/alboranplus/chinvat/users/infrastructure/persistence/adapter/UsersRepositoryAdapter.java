package eu.alboranplus.chinvat.users.infrastructure.persistence.adapter;

import eu.alboranplus.chinvat.users.application.port.out.UsersRepositoryPort;
import eu.alboranplus.chinvat.users.domain.model.UserAccount;
import eu.alboranplus.chinvat.users.domain.vo.UserEmail;
import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import eu.alboranplus.chinvat.users.infrastructure.persistence.jpa.UserAccountJpaRepository;
import eu.alboranplus.chinvat.users.infrastructure.persistence.mapper.UserAccountJpaMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UsersRepositoryAdapter implements UsersRepositoryPort {

  private final UserAccountJpaRepository userAccountJpaRepository;
  private final UserAccountJpaMapper userAccountJpaMapper;

  public UsersRepositoryAdapter(
      UserAccountJpaRepository userAccountJpaRepository,
      UserAccountJpaMapper userAccountJpaMapper) {
    this.userAccountJpaRepository = userAccountJpaRepository;
    this.userAccountJpaMapper = userAccountJpaMapper;
  }

  @Override
  public boolean existsByEmail(UserEmail email) {
    return userAccountJpaRepository.existsByEmailIgnoreCase(email.value());
  }

  @Override
  public Optional<UserAccount> findByEmail(UserEmail email) {
    return userAccountJpaRepository
        .findByEmailIgnoreCase(email.value())
        .map(userAccountJpaMapper::toDomain);
  }

  @Override
  public UserAccount save(UserAccount userAccount) {
    UserAccountJpaEntity persisted =
        userAccountJpaRepository.save(userAccountJpaMapper.toEntity(userAccount));
    return userAccountJpaMapper.toDomain(persisted);
  }
}
