package eu.alboranplus.chinvat.profile.api;

import eu.alboranplus.chinvat.profile.application.ProfileValidationException;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProfileApiExceptionHandler {

  @ExceptionHandler(ProfileValidationException.class)
  public ResponseEntity<ProfileErrorResponse> handleValidation(ProfileValidationException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ProfileErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  public ResponseEntity<ProfileErrorResponse> handleDomainState(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ProfileErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(CertificateCredentialNotFoundException.class)
  public ResponseEntity<ProfileErrorResponse> handleCredentialNotFound(
      CertificateCredentialNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ProfileErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProfileErrorResponse> handleMethodValidation(
      MethodArgumentNotValidException exception) {
    FieldError firstError = exception.getFieldErrors().stream().findFirst().orElse(null);
    String message = firstError == null ? "Validation failed" : firstError.getDefaultMessage();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ProfileErrorResponse(message, Instant.now()));
  }

  public record ProfileErrorResponse(String message, Instant timestamp) {}
}
