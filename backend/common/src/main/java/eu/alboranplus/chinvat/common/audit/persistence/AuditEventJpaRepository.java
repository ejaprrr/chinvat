package eu.alboranplus.chinvat.common.audit.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {}