package eu.alboranplus.chinvat.common.api.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public final class ApiErrorFactory {

  private ApiErrorFactory() {}

  public static ResponseEntity<ApiErrorResponse> build(
      HttpStatus status, ApiErrorCode code, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(code.code(), code.messageKey(), resolveMessage(message, code), Instant.now(), pathOf(request), List.of()));
  }

  public static ResponseEntity<ApiErrorResponse> build(
      HttpStatus status,
      ApiErrorCode code,
      String message,
      HttpServletRequest request,
      List<ApiErrorDetail> details) {
    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(code.code(), code.messageKey(), resolveMessage(message, code), Instant.now(), pathOf(request), details));
  }

  public static ResponseEntity<ApiErrorResponse> buildValidation(
      MethodArgumentNotValidException exception,
      ApiErrorCode code,
      HttpServletRequest request,
      HttpStatus status) {
    List<ApiErrorDetail> details =
        exception.getFieldErrors().stream().map(ApiErrorFactory::toDetail).toList();

    String message =
        details.stream().findFirst().map(ApiErrorDetail::message).orElse(code.defaultMessage());

    return build(status, code, message, request, details);
  }

  private static ApiErrorDetail toDetail(FieldError fieldError) {
    String rejectedValue = Objects.toString(fieldError.getRejectedValue(), null);
    return new ApiErrorDetail(fieldError.getField(), fieldError.getDefaultMessage(), rejectedValue);
  }

  private static String pathOf(HttpServletRequest request) {
    return request == null ? null : request.getRequestURI();
  }

  private static String resolveMessage(String message, ApiErrorCode code) {
    return message == null || message.isBlank() ? code.defaultMessage() : message;
  }
}
