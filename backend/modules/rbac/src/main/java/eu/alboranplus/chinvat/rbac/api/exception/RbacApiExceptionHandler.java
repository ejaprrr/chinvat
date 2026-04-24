package eu.alboranplus.chinvat.rbac.api.exception;

import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RbacApiExceptionHandler {

  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<RbacErrorResponse> handleNotFound(RoleNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new RbacErrorResponse(exception.getMessage(), Instant.now()));
  }

  public record RbacErrorResponse(String message, Instant timestamp) {}
}
