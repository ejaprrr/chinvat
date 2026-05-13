package eu.alboranplus.chinvat.rbac.api.exception;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionAlreadyExistsException;
import eu.alboranplus.chinvat.rbac.domain.exception.PermissionNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.RoleNotFoundException;
import eu.alboranplus.chinvat.rbac.domain.exception.UserNotFoundException;
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
public class RbacApiExceptionHandler {

  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      RoleNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.RBAC_ROLE_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(PermissionNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handlePermissionNotFound(
      PermissionNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND,
        ApiErrorCode.RBAC_PERMISSION_NOT_FOUND,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleUserNotFound(
      UserNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.RBAC_USER_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(PermissionAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handlePermissionAlreadyExists(
      PermissionAlreadyExistsException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.CONFLICT,
        ApiErrorCode.RBAC_PERMISSION_ALREADY_EXISTS,
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
