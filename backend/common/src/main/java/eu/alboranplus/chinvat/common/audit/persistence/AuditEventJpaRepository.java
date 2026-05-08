package eu.alboranplus.chinvat.common.audit.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, Long> {}