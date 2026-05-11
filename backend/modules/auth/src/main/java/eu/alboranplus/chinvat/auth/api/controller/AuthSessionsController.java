package eu.alboranplus.chinvat.auth.api.controller;

import eu.alboranplus.chinvat.auth.api.dto.AuthSessionResponse;
import eu.alboranplus.chinvat.auth.api.mapper.AuthApiMapper;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import jakarta.validation.constraints.NotNull;
import eu.alboranplus.chinvat.auth.application.dto.AuthSessionView;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Authentication", description = "Authentication: login, register, token refresh, password management, logout, and user profile")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthSessionsController {

  private final AuthFacade authFacade;
  private final AuthApiMapper authApiMapper;

  public AuthSessionsController(AuthFacade authFacade, AuthApiMapper authApiMapper) {
    this.authFacade = authFacade;
    this.authApiMapper = authApiMapper;
  }

  @Operation(
      summary = "List active sessions",
      description = "Returns all currently active sessions for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Active sessions",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthSessionResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/sessions")
  public ResponseEntity<List<AuthSessionResponse>> list(Authentication authentication) {
    TokenPrincipal principal = principal(authentication);
    return ResponseEntity.ok(authFacade.listSessions(principal).stream().map(authApiMapper::toSessionResponse).toList());
  }

  @Operation(
      summary = "Revoke a single active session",
      description = "Revokes one active session by session ID for the authenticated user.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Session revoked"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Session not found")
  })
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/sessions/{sessionId}")
  public ResponseEntity<Void> revoke(
      Authentication authentication,
      @Parameter(description = "Session UUID to revoke") @PathVariable @NotNull UUID sessionId) {
    TokenPrincipal principal = principal(authentication);
    authFacade.revokeSession(principal, sessionId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Revoke all active sessions",
      description = "Revokes all active sessions for the authenticated user.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "All sessions revoked"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/sessions")
  public ResponseEntity<Void> logoutAll(Authentication authentication) {
    TokenPrincipal principal = principal(authentication);
    authFacade.logoutAll(principal);
    return ResponseEntity.noContent().build();
  }

  private static TokenPrincipal principal(@NotNull Authentication authentication) {
    Object details = authentication.getDetails();
    if (details instanceof TokenPrincipal principal) {
      return principal;
    }
    throw new IllegalStateException("Token principal missing from authentication details");
  }
  
  @Operation(
      summary = "List active sessions with pagination",
      description = "Returns a paginated list of currently active sessions for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Paginated active sessions",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/sessions/paged")
  public ResponseEntity<PageResponse<AuthSessionResponse>> listSessionsPaged(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sort) {
    TokenPrincipal principal = principal(authentication);
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sort);
    PageResponse<AuthSessionView> pageResponse = authFacade.listSessionsPaged(principal, paginationRequest);
    
    List<AuthSessionResponse> responseData = pageResponse.data().stream()
        .map(authApiMapper::toSessionResponse).toList();
    
    return ResponseEntity.ok(PageResponse.of(responseData, pageResponse.pagination()));
  }
}

