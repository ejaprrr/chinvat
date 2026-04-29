package eu.alboranplus.chinvat.auth.api.controller;

import eu.alboranplus.chinvat.auth.api.dto.AuthMeResponse;
import eu.alboranplus.chinvat.auth.api.mapper.AuthApiMapper;
import eu.alboranplus.chinvat.auth.application.dto.AuthMeView;
import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Authentication (me)")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthMeController {

  private final AuthFacade authFacade;
  private final AuthApiMapper authApiMapper;

  public AuthMeController(AuthFacade authFacade, AuthApiMapper authApiMapper) {
    this.authFacade = authFacade;
    this.authApiMapper = authApiMapper;
  }

  @Operation(
      summary = "Get current user profile",
      description = "Returns profile information for the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Current user profile",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthMeResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/me")
  public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
    TokenPrincipal principal = principal(authentication);
    AuthMeView view = authFacade.me(principal);
    return ResponseEntity.ok(authApiMapper.toMeResponse(view));
  }

  private static TokenPrincipal principal(@NotNull Authentication authentication) {
    Object details = authentication.getDetails();
    if (details instanceof TokenPrincipal principal) {
      return principal;
    }
    throw new IllegalStateException("Token principal missing from authentication details");
  }
}

