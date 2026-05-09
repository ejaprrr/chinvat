package eu.alboranplus.chinvat.trust.infrastructure.persistence.mapper;

import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.infrastructure.persistence.entity.CertificateCredentialJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CertificateCredentialJpaMapper {

  public CertificateCredentialJpaEntity toEntity(CertificateCredentialView view) {
    CertificateCredentialJpaEntity entity = new CertificateCredentialJpaEntity();
    entity.setId(view.id());
    entity.setUserId(view.userId());
    entity.setProviderCode(view.providerCode());
    entity.setCredentialType(view.credentialType());
    entity.setTrustStatus(view.trustStatus());
    entity.setRevocationStatus(view.revocationStatus());
    entity.setAssuranceLevel(view.assuranceLevel());
    entity.setRegistrationSource(view.registrationSource());
    entity.setThumbprintSha256(view.thumbprintSha256());
    entity.setSubjectDn(view.subjectDn());
    entity.setIssuerDn(view.issuerDn());
    entity.setSerialNumber(view.serialNumber());
    entity.setCertificatePem(view.certificatePem());
    entity.setNotBefore(view.notBefore());
    entity.setNotAfter(view.notAfter());
    entity.setApprovedBy(view.approvedBy());
    entity.setApprovedAt(view.approvedAt());
    entity.setRevokedBy(view.revokedBy());
    entity.setRevokedAt(view.revokedAt());
    entity.setPrimary(view.primary());
    entity.setCreatedAt(view.createdAt());
    entity.setUpdatedAt(view.updatedAt());
    return entity;
  }

  public CertificateCredentialView toView(CertificateCredentialJpaEntity entity) {
    return new CertificateCredentialView(
        entity.getId(),
        entity.getUserId(),
        entity.getProviderCode(),
        entity.getCredentialType(),
        entity.getTrustStatus(),
        entity.getRevocationStatus(),
        entity.getAssuranceLevel(),
        entity.getRegistrationSource(),
        entity.getCertificatePem(),
        entity.getThumbprintSha256(),
        entity.getSubjectDn(),
        entity.getIssuerDn(),
        entity.getSerialNumber(),
        entity.getNotBefore(),
        entity.getNotAfter(),
        entity.getApprovedBy(),
        entity.getApprovedAt(),
        entity.getRevokedBy(),
        entity.getRevokedAt(),
        entity.isPrimary(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
