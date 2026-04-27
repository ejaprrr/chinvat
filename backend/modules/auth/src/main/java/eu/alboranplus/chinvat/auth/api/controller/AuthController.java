package eu.alboranplus.chinvat.auth.api.controller;

import eu.alboranplus.chinvat.auth.api.dto.AuthApiErrorResponse;
import eu.alboranplus.chinvat.auth.api.dto.AuthResponse;
import eu.alboranplus.chinvat.auth.api.dto.LoginRequest;
import eu.alboranplus.chinvat.auth.api.dto.LogoutRequest;
import eu.alboranplus.chinvat.auth.api.dto.RefreshRequest;
import eu.alboranplus.chinvat.auth.api.mapper.AuthApiMapper;
import eu.alboranplus.chinvat.auth.application.dto.AuthResult;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

