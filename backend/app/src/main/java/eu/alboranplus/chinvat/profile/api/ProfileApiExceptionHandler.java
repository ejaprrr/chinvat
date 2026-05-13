package eu.alboranplus.chinvat.profile.api;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.profile.application.ProfileValidationException;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ProfileController.class)
public class ProfileApiExceptionHandler {

  @ExceptionHandler(ProfileValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      ProfileValidationException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.BAD_REQUEST, ApiErrorCode.PROFILE_VALIDATION_FAILED, exception.getMessage(), request);
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiErrorResponse> handleDomainState(
      RuntimeException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.BAD_REQUEST, ApiErrorCode.PROFILE_INVALID_STATE, exception.getMessage(), request);
  }

  @ExceptionHandler(CertificateCredentialNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleCredentialNotFound(
      CertificateCredentialNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.PROFILE_CREDENTIAL_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ApiErrorFactory.buildValidation(
        exception, ApiErrorCode.COMMON_VALIDATION_FAILED, request, HttpStatus.BAD_REQUEST);
  }
}
