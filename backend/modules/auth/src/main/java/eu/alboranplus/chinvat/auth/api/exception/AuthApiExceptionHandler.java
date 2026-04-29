package eu.alboranplus.chinvat.auth.api.exception;

import eu.alboranplus.chinvat.auth.api.dto.AuthApiErrorResponse;
import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.exception.AuthResourceNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthApiExceptionHandler {

  @ExceptionHandler(InvalidAuthenticationException.class)
  public ResponseEntity<AuthApiErrorResponse> handleInvalidAuth(
      InvalidAuthenticationException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new AuthApiErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<AuthApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception) {
    FieldError firstError = exception.getFieldErrors().stream().findFirst().orElse(null);
    String message = firstError == null ? "Validation failed" : firstError.getDefaultMessage();
    return ResponseEntity.badRequest().body(new AuthApiErrorResponse(message, Instant.now()));
  }

  @ExceptionHandler(AuthResourceNotFoundException.class)
  public ResponseEntity<AuthApiErrorResponse> handleNotFound(AuthResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new AuthApiErrorResponse(ex.getMessage(), Instant.now()));
  }
}
