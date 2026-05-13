package eu.alboranplus.chinvat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import eu.alboranplus.chinvat.common.api.error.ApiErrorDetail;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiSecurityErrorWriter {

  private final ObjectMapper objectMapper;

  public ApiSecurityErrorWriter(ObjectMapper objectMapper) {
    this.objectMapper =
        objectMapper
            .copy()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public void write(
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String message,
      String path)
      throws IOException {
    write(response, status, code, message, path, List.of());
  }

  public void write(
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String message,
      String path,
      List<ApiErrorDetail> details)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ApiErrorResponse body =
        new ApiErrorResponse(
            code.code(),
            code.messageKey(),
            message == null || message.isBlank() ? code.defaultMessage() : message,
            Instant.now(),
            path,
            details);

    objectMapper.writeValue(response.getWriter(), body);
  }
}
