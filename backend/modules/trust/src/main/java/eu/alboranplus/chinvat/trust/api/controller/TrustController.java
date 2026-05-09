package eu.alboranplus.chinvat.trust.api.controller;

import eu.alboranplus.chinvat.trust.api.dto.SyncTrustedProvidersRequest;
import eu.alboranplus.chinvat.trust.api.dto.SyncTrustedProvidersResponse;
import eu.alboranplus.chinvat.trust.api.dto.ValidateCertificateRequest;
import eu.alboranplus.chinvat.trust.api.dto.ValidateCertificateResponse;
import eu.alboranplus.chinvat.trust.api.mapper.TrustApiMapper;
import eu.alboranplus.chinvat.trust.application.facade.TrustFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Trust", description = "Enterprise trust services: certificate validation and TSL/LOTL synchronization")
@RestController
@RequestMapping("/api/v1/trust")
public class TrustController {

  private final TrustFacade trustFacade;
  private final TrustApiMapper trustApiMapper;

  public TrustController(TrustFacade trustFacade, TrustApiMapper trustApiMapper) {
    this.trustFacade = trustFacade;
    this.trustApiMapper = trustApiMapper;
  }

  @Operation(
      summary = "Validate certificate",
      description =
          "Parses and validates an X.509 certificate and evaluates its trust status against the currently synchronized trusted providers.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Certificate validated",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidateCertificateResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid certificate payload")
  })
  @PostMapping("/certificates/validate")
  public ResponseEntity<ValidateCertificateResponse> validateCertificate(
      @Valid @RequestBody ValidateCertificateRequest request) {
    var command = trustApiMapper.toCommand(request);
    var result = trustFacade.validateCertificate(command);
    return ResponseEntity.ok(trustApiMapper.toResponse(result));
  }

  @Operation(
      summary = "Synchronize trusted providers",
      description =
          "Refreshes trusted providers from the configured EU LOTL source using official DSS synchronization.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Synchronization completed",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SyncTrustedProvidersResponse.class))),
    @ApiResponse(responseCode = "503", description = "Provider synchronization failed")
  })
  @PostMapping("/tsl/sync")
  public ResponseEntity<SyncTrustedProvidersResponse> synchronizeTrustedProviders(
      @RequestBody(required = false) SyncTrustedProvidersRequest request) {
    var command = trustApiMapper.toCommand(request);
    var result = trustFacade.syncTrustedProviders(command);
    return ResponseEntity.ok(trustApiMapper.toResponse(result));
  }
}
