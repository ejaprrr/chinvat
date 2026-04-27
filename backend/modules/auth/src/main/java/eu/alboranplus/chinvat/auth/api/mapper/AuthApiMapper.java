package eu.alboranplus.chinvat.auth.api.mapper;

import eu.alboranplus.chinvat.auth.api.dto.AuthResponse;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import eu.alboranplus.chinvat.auth.api.dto.LogoutRequest;
import eu.alboranplus.chinvat.auth.api.dto.RefreshRequest;
import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import org.springframework.stereotype.Component;

@Component
public class AuthApiMapper {

  public LoginCommand toCommand(LoginRequest request, String clientIp, String userAgent) {
    return new LoginCommand(request.email(), request.password(), clientIp, userAgent);
  }

  public RefreshCommand toRefreshCommand(
      RefreshRequest request, String clientIp, String userAgent) {
    return new RefreshCommand(request.refreshToken(), clientIp, userAgent);
  }

  public LogoutCommand toLogoutCommand(LogoutRequest request) {
    return new LogoutCommand(request.accessToken(), request.refreshToken());
  }

  public AuthResponse toResponse(AuthResult authResult) {
    AuthResponse.UserInfo userInfo =
        new AuthResponse.UserInfo(
            authResult.userId(),
            authResult.email(),
            authResult.displayName(),
            authResult.roles(),
            authResult.permissions());

    AuthResponse.TokenInfo tokenInfo =
        new AuthResponse.TokenInfo(
            authResult.tokens().accessToken(),
            authResult.tokens().refreshToken(),
            authResult.tokens().expiresAt());

    return new AuthResponse(userInfo, tokenInfo);
  }
}

