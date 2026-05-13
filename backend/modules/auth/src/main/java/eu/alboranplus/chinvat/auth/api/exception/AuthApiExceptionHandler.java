package eu.alboranplus.chinvat.auth.api.exception;

import eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException;
import eu.alboranplus.chinvat.auth.domain.exception.AuthResourceNotFoundException;
import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
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
public class AuthApiExceptionHandler {

  @ExceptionHandler(InvalidAuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidAuth(
      InvalidAuthenticationException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.UNAUTHORIZED,
        ApiErrorCode.AUTH_INVALID_AUTHENTICATION,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    return ApiErrorFactory.buildValidation(
        exception, ApiErrorCode.COMMON_VALIDATION_FAILED, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      AuthResourceNotFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND, ApiErrorCode.AUTH_RESOURCE_NOT_FOUND, exception.getMessage(), request);
  }
}
