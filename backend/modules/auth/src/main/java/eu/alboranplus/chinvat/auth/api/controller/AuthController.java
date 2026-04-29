package eu.alboranplus.chinvat.auth.api.controller;

import eu.alboranplus.chinvat.auth.api.dto.AuthApiErrorResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthSessionResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthMeResponse;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetConfirmRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequestResponse;
import eu.alboranplus.chinvat.auth.api.dto.RegisterRequest;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import eu.alboranplus.chinvat.auth.api.dto.LogoutRequest;
import eu.alboranplus.chinvat.auth.api.dto.RefreshRequest;
import eu.alboranplus.chinvat.auth.api.mapper.AuthApiMapper;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.dto.PasswordResetRequestResult;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import eu.alboranplus.chinvat.auth.application.command.ConfirmPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RequestPasswordResetCommand;
import eu.alboranplus.chinvat.auth.application.command.RegisterCommand;
import eu.alboranplus.chinvat.auth.application.command.LoginCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Auth", description = "Authentication: login, token refresh and logout")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthFacade authFacade;
  private final AuthApiMapper authApiMapper;

  public AuthController(AuthFacade authFacade, AuthApiMapper authApiMapper) {
    this.authFacade = authFacade;
    this.authApiMapper = authApiMapper;
  }

  @Operation(
      summary = "Login",
      description =
          "Authenticates user credentials and returns an access + refresh token pair."
              + " No authentication required.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Authenticated successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid credentials",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthApiErrorResponse.class)))
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");
    AuthResult result = authFacade.login(authApiMapper.toCommand(request, clientIp, userAgent));
    return ResponseEntity.ok(authApiMapper.toResponse(result));
  }

  @Operation(
      summary = "Register",
      description = "Creates a new user account and immediately issues access + refresh tokens.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Email or username already registered",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthApiErrorResponse.class)))
  })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");
    var result = authFacade.register(authApiMapper.toRegisterCommand(request, clientIp, userAgent));
    return ResponseEntity.status(HttpStatus.CREATED).body(authApiMapper.toResponse(result));
  }

  @Operation(
      summary = "Password reset request",
      description = "Requests a password reset token for the given email.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "202",
        description = "Password reset requested (no email enumeration).",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordResetRequestResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthApiErrorResponse.class)))
  })
  @PostMapping("/password-reset/request")
  public ResponseEntity<PasswordResetRequestResponse> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    // In local/test profile, the token can be returned for integration/e2e tests.
    boolean revealToken = Boolean.parseBoolean(
        httpRequest.getHeader("X-Debug-Reveal-Reset-Token") != null ? "true" : "false");

    var result =
        authFacade.requestPasswordReset(
            authApiMapper.toRequestPasswordResetCommand(request, revealToken, clientIp, userAgent));
    return ResponseEntity.accepted().body(authApiMapper.toPasswordResetRequestResponse(result));
  }

  @Operation(
      summary = "Password reset confirm",
      description = "Consumes the password reset token and updates the user password.",
      security = {})
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Password updated successfully"),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid or expired reset token",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthApiErrorResponse.class)))
  })
  @PostMapping("/password-reset/confirm")
  public ResponseEntity<Void> confirmPasswordReset(
      @Valid @RequestBody PasswordResetConfirmRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    authFacade.confirmPasswordReset(
        authApiMapper.toConfirmPasswordResetCommand(request, clientIp, userAgent));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Refresh tokens",
      description =
          "Issues a new access + refresh token pair in exchange for a valid refresh token."
              + " No authentication required.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Tokens refreshed successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Refresh token invalid or expired",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthApiErrorResponse.class)))
  })
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(
      @Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");
    AuthResult result =
        authFacade.refresh(authApiMapper.toRefreshCommand(request, clientIp, userAgent));
    return ResponseEntity.ok(authApiMapper.toResponse(result));
  }

  @Operation(
      summary = "Logout",
      description = "Revokes the session identified by the provided access and refresh tokens.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Logged out successfully"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(hidden = true)))
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
    authFacade.logout(authApiMapper.toLogoutCommand(request));
    return ResponseEntity.noContent().build();
  }

  private static String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}

