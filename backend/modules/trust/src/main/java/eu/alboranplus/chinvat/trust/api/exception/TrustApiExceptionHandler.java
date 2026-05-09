package eu.alboranplus.chinvat.trust.api.exception;

import eu.alboranplus.chinvat.trust.api.dto.TrustApiErrorResponse;
import eu.alboranplus.chinvat.trust.domain.exception.TrustProviderSyncException;
import eu.alboranplus.chinvat.trust.domain.exception.TrustValidationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TrustApiExceptionHandler {

  @ExceptionHandler(TrustValidationException.class)
  public ResponseEntity<TrustApiErrorResponse> handleValidation(TrustValidationException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new TrustApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(TrustProviderSyncException.class)
  public ResponseEntity<TrustApiErrorResponse> handleSync(TrustProviderSyncException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new TrustApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<TrustApiErrorResponse> handleMethodValidation(
      MethodArgumentNotValidException exception) {
    FieldError firstError = exception.getFieldErrors().stream().findFirst().orElse(null);
    String message = firstError == null ? "Validation failed" : firstError.getDefaultMessage();
    return ResponseEntity.badRequest().body(new TrustApiErrorResponse(message, Instant.now()));
  }
}
