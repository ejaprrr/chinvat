package eu.alboranplus.chinvat.eidas.api.exception;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasBrokerException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasExternalIdentityNotFoundException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasInvalidStateException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProfileCompletionException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProviderNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EidasApiExceptionHandler {

  @ExceptionHandler(EidasProviderNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleProviderNotFound(
      EidasProviderNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.EIDAS_PROVIDER_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(EidasInvalidStateException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidState(
      EidasInvalidStateException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.UNAUTHORIZED, ApiErrorCode.EIDAS_INVALID_STATE, exception.getMessage(), request);
  }

  @ExceptionHandler(EidasBrokerException.class)
  public ResponseEntity<ApiErrorResponse> handleBroker(
      EidasBrokerException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.SERVICE_UNAVAILABLE,
        ApiErrorCode.EIDAS_BROKER_UNAVAILABLE,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(EidasExternalIdentityNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleExternalIdentityNotFound(
      EidasExternalIdentityNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND,
        ApiErrorCode.EIDAS_EXTERNAL_IDENTITY_NOT_FOUND,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(EidasProfileCompletionException.class)
  public ResponseEntity<ApiErrorResponse> handleProfileCompletion(
      EidasProfileCompletionException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.CONFLICT,
        ApiErrorCode.EIDAS_PROFILE_COMPLETION_REQUIRED,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ApiErrorFactory.buildValidation(
        exception, ApiErrorCode.COMMON_VALIDATION_FAILED, request, HttpStatus.BAD_REQUEST);
  }
}
