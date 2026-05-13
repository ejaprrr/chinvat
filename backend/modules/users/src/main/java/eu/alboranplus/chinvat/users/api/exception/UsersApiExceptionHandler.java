package eu.alboranplus.chinvat.users.api.exception;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.users.domain.exception.UserAlreadyExistsException;
import eu.alboranplus.chinvat.users.domain.exception.UserNotFoundException;
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
public class UsersApiExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleAlreadyExists(
      UserAlreadyExistsException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.CONFLICT, ApiErrorCode.USERS_ALREADY_EXISTS, exception.getMessage(), request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      UserNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.USERS_NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ApiErrorFactory.buildValidation(
        exception, ApiErrorCode.COMMON_VALIDATION_FAILED, request, HttpStatus.BAD_REQUEST);
  }
}

