package eu.alboranplus.chinvat.rbac.api.exception;

import eu.alboranplus.chinvat.rbac.domain.exception.PermissionAlreadyExistsException;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.UserNotFoundException;
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

  @ExceptionHandler(PermissionNotFoundException.class)
  public ResponseEntity<RbacErrorResponse> handlePermissionNotFound(
      PermissionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new RbacErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<RbacErrorResponse> handleUserNotFound(UserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new RbacErrorResponse(exception.getMessage(), Instant.now()));
  }

  @ExceptionHandler(PermissionAlreadyExistsException.class)
  public ResponseEntity<RbacErrorResponse> handlePermissionAlreadyExists(
      PermissionAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new RbacErrorResponse(exception.getMessage(), Instant.now()));
  }

  public record RbacErrorResponse(String message, Instant timestamp) {}
}
