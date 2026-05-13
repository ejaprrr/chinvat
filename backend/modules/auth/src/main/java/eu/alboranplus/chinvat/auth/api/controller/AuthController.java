package eu.alboranplus.chinvat.auth.api.controller;

import eu.alboranplus.chinvat.auth.api.dto.AuthResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthSessionResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthMeResponse;
import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.auth.api.dto.PasswordChangeRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetConfirmRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequest;
import eu.alboranplus.chinvat.auth.api.dto.PasswordResetRequestResponse;
import eu.alboranplus.chinvat.auth.api.security.MtlsClientCertificateResolver;
import eu.alboranplus.chinvat.auth.api.dto.RegisterRequest;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import eu.alboranplus.chinvat.auth.api.dto.LogoutRequest;
import eu.alboranplus.chinvat.auth.api.dto.RefreshRequest;
import eu.alboranplus.chinvat.auth.api.mapper.AuthApiMapper;
import eu.alboranplus.chinvat.auth.application.command.CertificateLoginCommand;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Authentication", description = "Authentication: login, register, token refresh, password management, logout, and user profile")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthFacade authFacade;
  private final AuthApiMapper authApiMapper;
    private final MtlsClientCertificateResolver mtlsClientCertificateResolver;

    public AuthController(
            AuthFacade authFacade,
            AuthApiMapper authApiMapper,
            MtlsClientCertificateResolver mtlsClientCertificateResolver) {
    this.authFacade = authFacade;
    this.authApiMapper = authApiMapper;
        this.mtlsClientCertificateResolver = mtlsClientCertificateResolver;
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
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid credentials",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
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
            summary = "Client certificate login",
            description =
                "Authenticates the caller using a client certificate already validated by the mTLS gateway."
                    + " No request body is required; the gateway forwards the verified certificate.",
            security = {})
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Authenticated successfully with client certificate",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Client certificate missing, invalid, or not registered",
                content =
                        @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ApiErrorResponse.class)))
    })
            @PostMapping("/certificates/login")
            public ResponseEntity<AuthResponse> loginWithCertificate(HttpServletRequest httpRequest) {
            String clientIp = resolveClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String thumbprintSha256 = mtlsClientCertificateResolver.resolveThumbprintSha256(httpRequest);
            AuthResult result =
                authFacade.loginWithCertificate(
                    new CertificateLoginCommand(thumbprintSha256, clientIp, userAgent));
            return ResponseEntity.ok(authApiMapper.toResponse(result));
            }

            @Operation(
                summary = "FNMT certificate login (compatibility)",
                description =
                    "Compatibility alias for legacy FNMT clients. Prefer /api/v1/auth/certificates/login.",
                security = {})
            @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "Authenticated successfully with client certificate",
                content =
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(
                responseCode = "401",
                description = "Client certificate missing, invalid, or not registered",
                content =
                    @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping("/fnmt/login")
            public ResponseEntity<AuthResponse> loginWithFnmtAlias(HttpServletRequest httpRequest) {
            return loginWithCertificate(httpRequest);
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
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Email or username already registered",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
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
      description = "Requests a six-digit password reset code for the given email.",
      security = {})
  @ApiResponses({
    @ApiResponse(
        responseCode = "202",
        description = "Password reset requested (no email enumeration).",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = PasswordResetRequestResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @PostMapping("/password-reset/request")
  public ResponseEntity<PasswordResetRequestResponse> requestPasswordReset(
      @Valid @RequestBody PasswordResetRequest request, HttpServletRequest httpRequest) {
    String clientIp = resolveClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    // In local/test profile, the code can be returned for integration/e2e tests.
    boolean revealCode =
        httpRequest.getHeader("X-Debug-Reveal-Reset-Code") != null
            || httpRequest.getHeader("X-Debug-Reveal-Reset-Token") != null;

    var result =
        authFacade.requestPasswordReset(
            authApiMapper.toRequestPasswordResetCommand(request, revealCode, clientIp, userAgent));
    return ResponseEntity.accepted().body(authApiMapper.toPasswordResetRequestResponse(result));
  }

  @Operation(
      summary = "Password reset confirm",
      description = "Consumes the six-digit password reset code and updates the user password.",
      security = {})
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Password updated successfully"),
    @ApiResponse(
        responseCode = "401",
        description = "Invalid or expired reset code",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
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
            summary = "Change password",
            description = "Changes the current user's password using the current password.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password changed successfully"),
        @ApiResponse(
                responseCode = "400",
                description = "Validation failed",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Current password invalid",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(
            Authentication authentication, @Valid @RequestBody PasswordChangeRequest request) {
        authFacade.changePassword(principal(authentication), authApiMapper.toChangePasswordCommand(request));
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
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Refresh token invalid or expired",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
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
                schema = @Schema(implementation = ApiErrorResponse.class))),
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

    private static eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal principal(
            Authentication authentication) {
        Object details = authentication == null ? null : authentication.getDetails();
        if (details instanceof eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal tokenPrincipal) {
            return tokenPrincipal;
        }
        throw new eu.alboranplus.chinvat.auth.domain.exception.InvalidAuthenticationException(
                "Authenticated principal is not available");
    }

  private static String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}

