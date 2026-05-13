package eu.alboranplus.chinvat.security;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

  private final ApiSecurityErrorWriter errorWriter;

  public ApiAccessDeniedHandler(ApiSecurityErrorWriter errorWriter) {
    this.errorWriter = errorWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    errorWriter.write(
        response,
        HttpStatus.FORBIDDEN,
        ApiErrorCode.COMMON_FORBIDDEN,
        ApiErrorCode.COMMON_FORBIDDEN.defaultMessage(),
        request.getRequestURI());
  }
}
