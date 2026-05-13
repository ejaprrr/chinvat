package eu.alboranplus.chinvat.security;

import eu.alboranplus.chinvat.common.api.error.ApiErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ApiSecurityErrorWriter errorWriter;

  public ApiAuthenticationEntryPoint(ApiSecurityErrorWriter errorWriter) {
    this.errorWriter = errorWriter;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    errorWriter.write(
        response,
        HttpStatus.UNAUTHORIZED,
        ApiErrorCode.COMMON_UNAUTHORIZED,
        ApiErrorCode.COMMON_UNAUTHORIZED.defaultMessage(),
        request.getRequestURI());
  }
}
