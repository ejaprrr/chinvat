package eu.alboranplus.chinvat.trust.api.exception;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import eu.alboranplus.chinvat.trust.domain.exception.TrustProviderSyncException;
import eu.alboranplus.chinvat.trust.domain.exception.TrustValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrustApiExceptionHandler {

  @ExceptionHandler(TrustValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      TrustValidationException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.BAD_REQUEST, ApiErrorCode.TRUST_VALIDATION_FAILED, exception.getMessage(), request);
  }

  @ExceptionHandler(TrustProviderSyncException.class)
  public ResponseEntity<ApiErrorResponse> handleSync(
      TrustProviderSyncException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.SERVICE_UNAVAILABLE,
        ApiErrorCode.TRUST_PROVIDER_SYNC_FAILED,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(CertificateCredentialNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      CertificateCredentialNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.TRUST_CREDENTIAL_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ApiErrorFactory.buildValidation(
        exception, ApiErrorCode.COMMON_VALIDATION_FAILED, request, HttpStatus.BAD_REQUEST);
  }
}
