package eu.alboranplus.chinvat.api;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorFactory;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalApiExceptionHandler {

  private static final Logger log = Logger.getLogger(GlobalApiExceptionHandler.class.getName());

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(
      Exception exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.BAD_REQUEST,
        ApiErrorCode.COMMON_VALIDATION_FAILED,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      NoResourceFoundException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.NOT_FOUND,
        ApiErrorCode.COMMON_RESOURCE_NOT_FOUND,
        "Resource not found",
        request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDenied(
      AccessDeniedException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.FORBIDDEN,
        ApiErrorCode.COMMON_FORBIDDEN,
        ApiErrorCode.COMMON_FORBIDDEN.defaultMessage(),
        request);
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiErrorResponse> handleRuntimeValidation(
      RuntimeException exception, HttpServletRequest request) {
    return ApiErrorFactory.build(
        HttpStatus.BAD_REQUEST,
        ApiErrorCode.COMMON_VALIDATION_FAILED,
        exception.getMessage(),
        request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    log.log(Level.SEVERE, "Unhandled API exception", exception);
    return ApiErrorFactory.build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.COMMON_INTERNAL_ERROR,
        ApiErrorCode.COMMON_INTERNAL_ERROR.defaultMessage(),
        request);
  }
}
