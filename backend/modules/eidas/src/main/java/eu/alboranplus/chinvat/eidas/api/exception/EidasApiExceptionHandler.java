package eu.alboranplus.chinvat.eidas.api.exception;

import eu.alboranplus.chinvat.eidas.api.dto.EidasApiErrorResponse;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasBrokerException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasExternalIdentityNotFoundException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasInvalidStateException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProfileCompletionException;
import eu.alboranplus.chinvat.eidas.domain.exception.EidasProviderNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EidasApiExceptionHandler {

  @ExceptionHandler(EidasProviderNotFoundException.class)
  public ResponseEntity<EidasApiErrorResponse> handleProviderNotFound(
      EidasProviderNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new EidasApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(EidasInvalidStateException.class)
  public ResponseEntity<EidasApiErrorResponse> handleInvalidState(
      EidasInvalidStateException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new EidasApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(EidasBrokerException.class)
  public ResponseEntity<EidasApiErrorResponse> handleBroker(EidasBrokerException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new EidasApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(EidasExternalIdentityNotFoundException.class)
  public ResponseEntity<EidasApiErrorResponse> handleExternalIdentityNotFound(
      EidasExternalIdentityNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new EidasApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(EidasProfileCompletionException.class)
  public ResponseEntity<EidasApiErrorResponse> handleProfileCompletion(
      EidasProfileCompletionException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new EidasApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<EidasApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception) {
    FieldError firstError = exception.getFieldErrors().stream().findFirst().orElse(null);
    String message = firstError == null ? "Validation failed" : firstError.getDefaultMessage();
    return ResponseEntity.badRequest().body(new EidasApiErrorResponse(message, Instant.now()));
  }
}
