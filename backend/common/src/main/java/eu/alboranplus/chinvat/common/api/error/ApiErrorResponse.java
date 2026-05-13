package eu.alboranplus.chinvat.common.api.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    String errorCode,
    String messageKey,
    String message,
    Instant timestamp,
    String path,
    List<ApiErrorDetail> details) {

  public ApiErrorResponse {
    details = details == null ? List.of() : List.copyOf(details);
  }
}
