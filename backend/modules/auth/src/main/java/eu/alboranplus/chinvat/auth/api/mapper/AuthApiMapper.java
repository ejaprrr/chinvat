package eu.alboranplus.chinvat.auth.api.mapper;

import eu.alboranplus.chinvat.auth.api.dto.AuthResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthMeResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthSessionResponse;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetConfirmRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequestResponse;
import eu.alboranplus.chinvat.auth.api.dto.RegisterRequest;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import eu.alboranplus.chinvat.auth.api.dto.LogoutRequest;
import eu.alboranplus.chinvat.auth.api.dto.RefreshRequest;
import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import eu.alboranplus.chinvat.auth.application.command.LogoutCommand;
import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.command.RefreshCommand;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
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

  public RegisterCommand toRegisterCommand(
      RegisterRequest request, String clientIp, String userAgent) {
    return new RegisterCommand(
        request.username(),
        request.fullName(),
        request.phoneNumber(),
        request.email(),
        request.password(),
        request.userType(),
        request.addressLine(),
        request.postalCode(),
        request.city(),
        request.country(),
        request.defaultLanguage(),
        clientIp,
        userAgent);
  }

  public RequestPasswordResetCommand toRequestPasswordResetCommand(
      PasswordResetRequest request, boolean revealToken, String clientIp, String userAgent) {
    return new RequestPasswordResetCommand(request.email(), clientIp, userAgent, revealToken);
  }

  public ConfirmPasswordResetCommand toConfirmPasswordResetCommand(
      PasswordResetConfirmRequest request, String clientIp, String userAgent) {
    return new ConfirmPasswordResetCommand(
        request.resetToken(), request.newPassword(), clientIp, userAgent);
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

  public AuthMeResponse toMeResponse(AuthMeView view) {
    return new AuthMeResponse(
        view.id(), view.email(), view.displayName(), view.roles(), view.permissions());
  }

  public AuthSessionResponse toSessionResponse(AuthSessionView view) {
    return new AuthSessionResponse(
        view.sessionId(),
        view.tokenKind(),
        view.issuedAt(),
        view.expiresAt(),
        view.clientIp(),
        view.userAgent());
  }

  public PasswordResetRequestResponse toPasswordResetRequestResponse(
      PasswordResetRequestResult result) {
    return new PasswordResetRequestResponse(result.resetToken());
  }
}

