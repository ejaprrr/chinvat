package eu.alboranplus.chinvat.trust.api.controller;

import eu.alboranplus.chinvat.common.api.error.ApiErrorResponse;
import eu.alboranplus.chinvat.trust.api.dto.BindCertificateCredentialRequest;
import eu.alboranplus.chinvat.trust.api.dto.CertificateCredentialResponse;
import eu.alboranplus.chinvat.trust.api.dto.RevokeCertificateCredentialRequest;
import eu.alboranplus.chinvat.trust.api.mapper.TrustApiMapper;
import eu.alboranplus.chinvat.trust.application.facade.TrustFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import eu.alboranplus.chinvat.common.pagination.PageResponse;
import eu.alboranplus.chinvat.common.pagination.PaginationRequest;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Credentials", description = "Administrative lifecycle operations for certificate credentials")
@RestController
@RequestMapping("/api/v1/admin/credentials")
public class AdminCredentialsController {

  private final TrustFacade trustFacade;
  private final TrustApiMapper trustApiMapper;

  public AdminCredentialsController(TrustFacade trustFacade, TrustApiMapper trustApiMapper) {
    this.trustFacade = trustFacade;
    this.trustApiMapper = trustApiMapper;
  }

  @Operation(summary = "Bind certificate credential", description = "Registers and approves a certificate credential for a user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Credential bound",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CertificateCredentialResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping
  @PreAuthorize("hasAuthority('USERS:MANAGE') or hasAuthority('RBAC:MANAGE')")
  public ResponseEntity<CertificateCredentialResponse> bindCertificateCredential(
      @Valid @RequestBody BindCertificateCredentialRequest request, Authentication authentication) {
    var result = trustFacade.bindCertificateCredential(trustApiMapper.toCommand(request), actor(authentication));
    return ResponseEntity.status(HttpStatus.CREATED).body(trustApiMapper.toResponse(result));
  }

  @Operation(summary = "List certificate credentials", description = "Returns certificate credentials, optionally filtered by userId.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Credential list returned")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping
  @PreAuthorize("hasAuthority('USERS:MANAGE') or hasAuthority('RBAC:MANAGE')")
  public ResponseEntity<PageResponse<CertificateCredentialResponse>> listCertificateCredentials(
      @Parameter(description = "Filter by user UUID", example = "550e8400-e29b-41d4-a716-446655440000")
          @RequestParam(required = false) UUID userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sort) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sort);
    PageResponse<CertificateCredentialView> pageResponse = trustFacade.listCertificateCredentialsPaged(userId, paginationRequest);
    List<CertificateCredentialResponse> responseData = pageResponse.data().stream()
        .map(trustApiMapper::toResponse).toList();
    return ResponseEntity.ok(PageResponse.of(responseData, pageResponse.pagination()));
  }

  @Operation(summary = "Revoke certificate credential", description = "Revokes a certificate credential and records the reason.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Credential revoked"),
    @ApiResponse(
        responseCode = "404",
        description = "Credential not found",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/{credentialId}/revoke")
  @PreAuthorize("hasAuthority('USERS:MANAGE') or hasAuthority('RBAC:MANAGE')")
  public ResponseEntity<Void> revokeCertificateCredential(
      @Parameter(description = "Credential UUID", example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable UUID credentialId,
      @Valid @RequestBody RevokeCertificateCredentialRequest request,
      Authentication authentication) {
    trustFacade.revokeCertificateCredential(credentialId, actor(authentication), request.reason());
    return ResponseEntity.noContent().build();
  }

  private static String actor(Authentication authentication) {
    return authentication == null ? "system" : authentication.getName();
  }
}
