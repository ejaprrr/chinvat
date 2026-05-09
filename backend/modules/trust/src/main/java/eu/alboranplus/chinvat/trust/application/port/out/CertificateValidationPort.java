package eu.alboranplus.chinvat.trust.application.port.out;

import eu.alboranplus.chinvat.trust.application.dto.CertificateValidationResult;

public interface CertificateValidationPort {
  CertificateValidationResult validate(String certificatePem);
}
