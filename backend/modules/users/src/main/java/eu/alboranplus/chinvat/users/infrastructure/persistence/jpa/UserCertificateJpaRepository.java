package eu.alboranplus.chinvat.users.infrastructure.persistence.jpa;

import eu.alboranplus.chinvat.users.infrastructure.persistence.entity.UserCertificateJpaEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCertificateJpaRepository extends JpaRepository<UserCertificateJpaEntity, Long> {

  @Query(
      """
      select certificate.userId
      from UserCertificateJpaEntity certificate
      where upper(certificate.thumbprintSha256) = upper(:thumbprintSha256)
        and certificate.revokedAt is null
        and certificate.notBefore <= :now
        and certificate.notAfter >= :now
      """)
  Optional<Long> findActiveUserIdByThumbprintSha256(
      @Param("thumbprintSha256") String thumbprintSha256, @Param("now") Instant now);
}