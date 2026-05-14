package eu.alboranplus.chinvat.profile.application;

import eu.alboranplus.chinvat.common.audit.AuditDetails;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.eidas.application.command.CompleteEidasProfileCommand;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProfileCompletionView;
import eu.alboranplus.chinvat.eidas.application.facade.EidasFacade;
import eu.alboranplus.chinvat.profile.application.command.AddProfileCertificateCommand;
import eu.alboranplus.chinvat.profile.application.command.CompleteProfileAfterEidasCommand;
import eu.alboranplus.chinvat.trust.application.command.BindCertificateCredentialCommand;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.facade.TrustFacade;
import eu.alboranplus.chinvat.users.application.command.CreateUserCommand;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

  private static final String PROFILE_REGISTRATION_SOURCE = "PROFILE_SELF_SERVICE";

  private final UsersFacade usersFacade;
  private final TrustFacade trustFacade;
  private final EidasFacade eidasFacade;
  private final AuditFacade auditFacade;

  public ProfileService(
      UsersFacade usersFacade,
      TrustFacade trustFacade,
      EidasFacade eidasFacade,
      AuditFacade auditFacade) {
    this.usersFacade = usersFacade;
    this.trustFacade = trustFacade;
    this.eidasFacade = eidasFacade;
    this.auditFacade = auditFacade;
  }

  public List<CertificateCredentialView> listCertificates(Authentication authentication) {
    return trustFacade.listCertificateCredentials(currentUserId(authentication));
  }

  public CertificateCredentialView addCertificate(
      AddProfileCertificateCommand command, Authentication authentication) {
    UUID userId = currentUserId(authentication);
    String actor = actor(authentication);
    CertificateCredentialView created =
        trustFacade.bindCertificateCredential(
            new BindCertificateCredentialCommand(
                userId,
          command.providerCode(),
                PROFILE_REGISTRATION_SOURCE,
          command.assuranceLevel(),
          command.certificatePem()),
            actor);

    auditFacade.log(
        "PROFILE_CERTIFICATE_ADDED",
        actor,
        userId,
        AuditDetails.builder()
            .add("credentialId", created.id())
            .add("thumbprintSha256", created.thumbprintSha256())
            .add("trustStatus", created.trustStatus())
            .build());

    return created;
  }

  public void removeCertificate(UUID credentialId, String reason, Authentication authentication) {
    UUID userId = currentUserId(authentication);
    String actor = actor(authentication);
    ensureOwnedByUser(userId, credentialId);

    trustFacade.revokeCertificateCredential(credentialId, actor, reason);
    auditFacade.log(
        "PROFILE_CERTIFICATE_REVOKED",
        actor,
        userId,
        AuditDetails.builder()
            .add("credentialId", credentialId)
            .add("reason", reason)
            .build());
  }

  public CertificateCredentialView setPrimaryCertificate(
      UUID credentialId, Authentication authentication) {
    UUID userId = currentUserId(authentication);
    String actor = actor(authentication);
    CertificateCredentialView primary =
        trustFacade.setPrimaryCertificateCredential(userId, credentialId, actor);

    auditFacade.log(
        "PROFILE_CERTIFICATE_PRIMARY_SET",
        actor,
        userId,
        AuditDetails.builder()
            .add("credentialId", primary.id())
            .add("thumbprintSha256", primary.thumbprintSha256())
            .build());

    return primary;
  }

  @Transactional
  public EidasProfileCompletionView completeEidasProfile(CompleteProfileAfterEidasCommand command) {
    UserView user =
        usersFacade.createUser(
            new CreateUserCommand(
                command.username(),
                command.fullName(),
                command.phoneNumber(),
                command.email(),
                command.password(),
                UserType.INDIVIDUAL,
                AccessLevel.NORMAL,
                command.addressLine(),
                command.postalCode(),
                command.city(),
                command.country(),
                command.defaultLanguage()));

    boolean hasCertificate =
        command.certificatePem() != null && !command.certificatePem().isBlank();

    CertificateCredentialView credential = null;
    if (hasCertificate) {
      credential =
          trustFacade.bindCertificateCredential(
              new BindCertificateCredentialCommand(
                  user.id(),
                command.certificateProviderCode(),
                  PROFILE_REGISTRATION_SOURCE,
                command.assuranceLevel(),
                command.certificatePem()),
              command.email());

      validateActivationReadiness(user, credential);
    }

    EidasProfileCompletionView completion =
        eidasFacade.completeProfile(
            new CompleteEidasProfileCommand(
              command.providerCode(),
              command.externalSubjectId(),
                user.id(),
              command.identityReference(),
              command.nationality()),
            command.email());

    if (credential != null && !credential.primary()) {
      trustFacade.setPrimaryCertificateCredential(user.id(), credential.id(), command.email());
    }

    AuditDetails.Builder auditBuilder =
        AuditDetails.builder()
            .add("providerCode", completion.providerCode())
            .add("externalSubjectId", completion.externalSubjectId())
            .add("currentStatus", completion.currentStatus());
    if (credential != null) {
      auditBuilder.add("credentialId", credential.id());
    }

    auditFacade.log(
        "PROFILE_COMPLETION_ACTIVATED",
        command.email(),
        user.id(),
        auditBuilder.build());

    return completion;
  }

  private void ensureOwnedByUser(UUID userId, UUID credentialId) {
    boolean owned =
        trustFacade.listCertificateCredentials(userId).stream()
            .anyMatch(credential -> credential.id().equals(credentialId));
    if (!owned) {
      throw new ProfileValidationException(
          "Certificate credential not found for current user: " + credentialId);
    }
  }

  private static void validateActivationReadiness(UserView user, CertificateCredentialView credential) {
    if (user.fullName() == null || user.fullName().isBlank()) {
      throw new ProfileValidationException("Full name is required before account activation");
    }
    if (user.defaultLanguage() == null || user.defaultLanguage().isBlank()) {
      throw new ProfileValidationException("Default language is required before account activation");
    }
    if (!"TRUSTED".equals(credential.trustStatus())) {
      throw new ProfileValidationException("Account activation requires a TRUSTED certificate");
    }
    if (!"ACTIVE".equals(credential.revocationStatus())) {
      throw new ProfileValidationException("Account activation requires an ACTIVE certificate");
    }

    Instant now = Instant.now();
    if (credential.notAfter().isBefore(now) || credential.notBefore().isAfter(now)) {
      throw new ProfileValidationException(
          "Account activation requires a certificate valid at current time");
    }
  }

  private UUID currentUserId(Authentication authentication) {
    return usersFacade
        .findSecurityViewByEmail(authentication.getName())
        .map(UserSecurityView::id)
        .orElseThrow(() -> new ProfileValidationException("Authenticated user does not exist"));
  }

  private static String actor(Authentication authentication) {
    return authentication == null ? "system" : authentication.getName();
  }
}
