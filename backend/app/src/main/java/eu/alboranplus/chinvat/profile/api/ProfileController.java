package eu.alboranplus.chinvat.profile.api;

import eu.alboranplus.chinvat.profile.api.dto.AddProfileCertificateRequest;
import eu.alboranplus.chinvat.profile.api.dto.CompleteEidasProfileRequest;
import eu.alboranplus.chinvat.profile.api.dto.CompleteEidasProfileResponse;
import eu.alboranplus.chinvat.profile.api.dto.ProfileCertificateResponse;
import eu.alboranplus.chinvat.profile.application.command.AddProfileCertificateCommand;
import eu.alboranplus.chinvat.profile.application.command.CompleteProfileAfterEidasCommand;
import eu.alboranplus.chinvat.profile.application.ProfileService;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "Profile completion and self-service certificate management")
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @Operation(
      summary = "Complete profile after eIDAS callback",
      description =
          "Creates user profile, binds certificate credential, validates activation rules and links pending eIDAS identity.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Profile completed and activated"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed",
        content = @Content(mediaType = "application/json", schema = @Schema(hidden = true))),
    @ApiResponse(responseCode = "404", description = "Pending external identity not found")
  })
  @PostMapping("/eidas/complete")
  public ResponseEntity<CompleteEidasProfileResponse> completeEidasProfile(
      @Valid @RequestBody CompleteEidasProfileRequest request) {
    var completion =
      profileService.completeEidasProfile(
        new CompleteProfileAfterEidasCommand(
          request.providerCode(),
          request.externalSubjectId(),
          request.username(),
          request.fullName(),
          request.phoneNumber(),
          request.email(),
          request.password(),
          request.addressLine(),
          request.postalCode(),
          request.city(),
          request.country(),
          request.defaultLanguage(),
          request.certificatePem(),
          request.assuranceLevel(),
          request.certificateProviderCode(),
          request.identityReference(),
          request.nationality()));
    return ResponseEntity.ok(
        new CompleteEidasProfileResponse(
            completion.userId(),
            completion.providerCode(),
            completion.externalSubjectId(),
            completion.currentStatus(),
            completion.linkedAt(),
            Instant.now()));
  }

  @Operation(summary = "List profile certificates")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Certificates returned"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/certificates")
  public ResponseEntity<List<ProfileCertificateResponse>> listCertificates(Authentication authentication) {
    List<ProfileCertificateResponse> response =
        profileService.listCertificates(authentication).stream()
            .map(this::toResponse)
            .toList();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Add profile certificate")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Certificate added"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/certificates")
  public ResponseEntity<ProfileCertificateResponse> addCertificate(
      @Valid @RequestBody AddProfileCertificateRequest request,
      Authentication authentication) {
    CertificateCredentialView created =
      profileService.addCertificate(
        new AddProfileCertificateCommand(
          request.certificatePem(), request.providerCode(), request.assuranceLevel()),
        authentication);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
  }

  @Operation(summary = "Remove profile certificate")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Certificate revoked"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "400", description = "Validation failed")
  })
  @SecurityRequirement(name = "bearerAuth")
  @DeleteMapping("/certificates/{credentialId}")
  public ResponseEntity<Void> removeCertificate(
      @PathVariable Long credentialId,
      @RequestParam(defaultValue = "USER_REQUEST") String reason,
      Authentication authentication) {
    profileService.removeCertificate(credentialId, reason, authentication);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Set primary profile certificate")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Primary certificate updated"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "400", description = "Validation failed")
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/certificates/{credentialId}/primary")
  public ResponseEntity<ProfileCertificateResponse> setPrimaryCertificate(
      @PathVariable Long credentialId,
      Authentication authentication) {
    CertificateCredentialView updated = profileService.setPrimaryCertificate(credentialId, authentication);
    return ResponseEntity.ok(toResponse(updated));
  }

  private ProfileCertificateResponse toResponse(CertificateCredentialView view) {
    return new ProfileCertificateResponse(
        view.id(),
        view.providerCode(),
        view.trustStatus(),
        view.revocationStatus(),
        view.assuranceLevel(),
        view.thumbprintSha256(),
        view.subjectDn(),
        view.issuerDn(),
        view.serialNumber(),
        view.notBefore(),
        view.notAfter(),
        view.primary(),
        view.createdAt(),
        view.updatedAt());
  }
}
