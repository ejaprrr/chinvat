package eu.alboranplus.chinvat.users.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserAccountJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
  
  // Soft delete aware queries - only return non-deleted users
  
  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccountJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
  boolean existsByEmailIgnoreCase(String email);

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserAccountJpaEntity u WHERE LOWER(u.username) = LOWER(:username) AND u.deletedAt IS NULL")
  boolean existsByUsernameIgnoreCase(String username);

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL")
  Optional<UserAccountJpaEntity> findByEmailIgnoreCase(String email);

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE LOWER(u.username) = LOWER(:username) AND u.deletedAt IS NULL")
  Optional<UserAccountJpaEntity> findByUsernameIgnoreCase(String username);

  // Enterprise soft delete queries
  
  @Query("SELECT u FROM UserAccountJpaEntity u WHERE u.deletedAt IS NULL")
  List<UserAccountJpaEntity> findAllActive();

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE u.deletedAt IS NULL")
  Page<UserAccountJpaEntity> findAllActive(Pageable pageable);

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<UserAccountJpaEntity> findByIdActive(UUID id);

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE u.deletedAt IS NOT NULL")
  List<UserAccountJpaEntity> findAllDeleted();

  @Query("SELECT u FROM UserAccountJpaEntity u WHERE u.id = :id AND u.deletedAt IS NOT NULL")
  Optional<UserAccountJpaEntity> findByIdDeleted(UUID id);
}

