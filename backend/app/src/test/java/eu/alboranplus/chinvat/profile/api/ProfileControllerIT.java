package eu.alboranplus.chinvat.profile.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.alboranplus.chinvat.auth.application.dto.TokenPrincipal;
import eu.alboranplus.chinvat.auth.application.facade.AuthFacade;
import eu.alboranplus.chinvat.common.audit.AuditFacade;
import eu.alboranplus.chinvat.eidas.application.dto.EidasProfileCompletionView;
import eu.alboranplus.chinvat.eidas.application.facade.EidasFacade;
import eu.alboranplus.chinvat.trust.application.dto.CertificateCredentialView;
import eu.alboranplus.chinvat.trust.application.facade.TrustFacade;
import eu.alboranplus.chinvat.trust.domain.exception.CertificateCredentialNotFoundException;
import eu.alboranplus.chinvat.users.application.dto.UserSecurityView;
import eu.alboranplus.chinvat.users.application.dto.UserView;
import eu.alboranplus.chinvat.users.application.facade.UsersFacade;
import eu.alboranplus.chinvat.users.domain.model.AccessLevel;
import eu.alboranplus.chinvat.users.domain.model.UserType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProfileControllerIT {

    private static final UUID UUID_77 = UUID.fromString("00000000-0000-0000-0000-00000000004d");
    private static final UUID UUID_501 = UUID.fromString("00000000-0000-0000-0000-0000000001f5");
    private static final UUID UUID_777 = UUID.fromString("00000000-0000-0000-0000-000000000309");
    private static final UUID UUID_999 = UUID.fromString("00000000-0000-0000-0000-0000000003e7");

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UsersFacade usersFacade;
  @MockitoBean private TrustFacade trustFacade;
  @MockitoBean private EidasFacade eidasFacade;
    @MockitoBean private AuthFacade authFacade;
    @MockitoBean private AuditFacade auditFacade;

  @Test
  void completeEidasProfile_createsUserBindsCertificateAndLinksIdentity() throws Exception {
    Instant now = Instant.now();

    given(usersFacade.createUser(any()))
        .willReturn(
            new UserView(
                UUID_77,
                "maria",
                "Maria Example",
                "+34 600 000 111",
                "maria@example.com",
                UserType.INDIVIDUAL,
                AccessLevel.NORMAL,
                "Street 1",
                "29001",
                "Malaga",
                "ES",
                "es"));

    given(trustFacade.bindCertificateCredential(any(), any()))
        .willReturn(
            new CertificateCredentialView(
                UUID_501,
                UUID_77,
                "FNMT",
                "CLIENT_TLS",
                "TRUSTED",
                "ACTIVE",
                "high",
                "PROFILE_SELF_SERVICE",
                "pem",
                "thumb-1",
                "subject",
                "issuer",
                "serial",
                now.minusSeconds(60),
                now.plusSeconds(3600),
                "maria@example.com",
                now,
                null,
                null,
                false,
                now,
                now));

    given(eidasFacade.completeProfile(any(), any()))
        .willReturn(
            new EidasProfileCompletionView("EIDAS_EU", "subject-1", UUID_77, "ACTIVE", now, now));

    given(trustFacade.setPrimaryCertificateCredential(UUID_77, UUID_501, "maria@example.com"))
        .willReturn(
            new CertificateCredentialView(
                UUID_501,
                UUID_77,
                "FNMT",
                "CLIENT_TLS",
                "TRUSTED",
                "ACTIVE",
                "high",
                "PROFILE_SELF_SERVICE",
                "pem",
                "thumb-1",
                "subject",
                "issuer",
                "serial",
                now.minusSeconds(60),
                now.plusSeconds(3600),
                "maria@example.com",
                now,
                null,
                null,
                true,
                now,
                now));

    mockMvc
        .perform(
            post("/api/v1/profile/eidas/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "providerCode": "EIDAS_EU",
                      "externalSubjectId": "subject-1",
                      "username": "maria",
                      "fullName": "Maria Example",
                      "phoneNumber": "+34 600 000 111",
                      "email": "maria@example.com",
                      "password": "StrongPassword123!",
                      "addressLine": "Street 1",
                      "postalCode": "29001",
                      "city": "Malaga",
                      "country": "ES",
                      "defaultLanguage": "es",
                      "certificatePem": "-----BEGIN CERTIFICATE-----MIIB...-----END CERTIFICATE-----",
                      "assuranceLevel": "high",
                      "certificateProviderCode": "FNMT",
                      "identityReference": "ES/1234567",
                      "nationality": "ES"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(UUID_77.toString()))
        .andExpect(jsonPath("$.providerCode").value("EIDAS_EU"))
        .andExpect(jsonPath("$.externalSubjectId").value("subject-1"))
        .andExpect(jsonPath("$.currentStatus").value("ACTIVE"));
  }

  @Test
  void certificateEndpoints_requireAuthAndSupportLifecycle() throws Exception {
        Instant now = Instant.now();
    given(authFacade.validateAccessToken("token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    UUID_77,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(
            Optional.of(
                new UserSecurityView(UUID_77, "maria@example.com", "Maria", Set.of("USER"), true)));

    given(trustFacade.listCertificateCredentials(UUID_77))
        .willReturn(
            List.of(
                new CertificateCredentialView(
                    UUID_501,
                    UUID_77,
                    "FNMT",
                    "CLIENT_TLS",
                    "TRUSTED",
                    "ACTIVE",
                    "high",
                    "PROFILE_SELF_SERVICE",
                    "pem",
                    "thumb-1",
                    "subject",
                    "issuer",
                    "serial",
                    now.minusSeconds(60),
                    now.plusSeconds(3600),
                    "maria@example.com",
                    now,
                    null,
                    null,
                    true,
                    now,
                    now)));

    given(trustFacade.bindCertificateCredential(any(), any()))
        .willReturn(
            new CertificateCredentialView(
                UUID_777,
                UUID_77,
                "FNMT",
                "CLIENT_TLS",
                "TRUSTED",
                "ACTIVE",
                "high",
                "PROFILE_SELF_SERVICE",
                "pem",
                "thumb-2",
                "subject",
                "issuer",
                "serial",
                now.minusSeconds(60),
                now.plusSeconds(3600),
                "maria@example.com",
                now,
                null,
                null,
                false,
                now,
                now));

    willDoNothing()
        .given(trustFacade)
        .revokeCertificateCredential(UUID_501, "maria@example.com", "ROTATED");

    given(trustFacade.setPrimaryCertificateCredential(UUID_77, UUID_501, "maria@example.com"))
        .willReturn(
            new CertificateCredentialView(
                UUID_501,
                UUID_77,
                "FNMT",
                "CLIENT_TLS",
                "TRUSTED",
                "ACTIVE",
                "high",
                "PROFILE_SELF_SERVICE",
                "pem",
                "thumb-1",
                "subject",
                "issuer",
                "serial",
                now.minusSeconds(60),
                now.plusSeconds(3600),
                "maria@example.com",
                now,
                null,
                null,
                true,
                now,
                now));

    mockMvc
        .perform(get("/api/v1/profile/certificates").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(UUID_501.toString()))
        .andExpect(jsonPath("$[0].primary").value(true));

    mockMvc
        .perform(
            post("/api/v1/profile/certificates")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "certificatePem": "-----BEGIN CERTIFICATE-----MIIB...-----END CERTIFICATE-----",
                      "providerCode": "FNMT",
                      "assuranceLevel": "high"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(UUID_777.toString()));

    mockMvc
        .perform(
            delete("/api/v1/profile/certificates/" + UUID_501)
                .queryParam("reason", "ROTATED")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/profile/certificates/" + UUID_501 + "/primary")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(UUID_501.toString()))
        .andExpect(jsonPath("$.primary").value(true));
  }

  @Test
  void setPrimaryCertificate_whenCredentialMissing_returns404() throws Exception {
    given(authFacade.validateAccessToken("token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    UUID_77,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(
            Optional.of(
                new UserSecurityView(UUID_77, "maria@example.com", "Maria", Set.of("USER"), true)));
    given(trustFacade.setPrimaryCertificateCredential(UUID_77, UUID_999, "maria@example.com"))
        .willThrow(
            new CertificateCredentialNotFoundException(
                "Certificate credential not found: " + UUID_999));

    mockMvc
        .perform(
            post("/api/v1/profile/certificates/" + UUID_999 + "/primary")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("PRF-404-001"))
        .andExpect(jsonPath("$.messageKey").value("error.profile.credential-not-found"))
        .andExpect(jsonPath("$.message").value("Certificate credential not found: " + UUID_999))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.path").value("/api/v1/profile/certificates/" + UUID_999 + "/primary"))
        .andExpect(jsonPath("$.details").isArray());
  }

  @Test
  void setPrimaryCertificate_whenCredentialNotActive_returns400() throws Exception {
    given(authFacade.validateAccessToken("token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    UUID_77,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(
            Optional.of(
                new UserSecurityView(UUID_77, "maria@example.com", "Maria", Set.of("USER"), true)));
    given(trustFacade.setPrimaryCertificateCredential(UUID_77, UUID_501, "maria@example.com"))
        .willThrow(new IllegalStateException("Only ACTIVE credentials can be primary"));

    mockMvc
        .perform(
            post("/api/v1/profile/certificates/" + UUID_501 + "/primary")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("PRF-400-002"))
        .andExpect(jsonPath("$.messageKey").value("error.profile.invalid-state"))
        .andExpect(jsonPath("$.message").value("Only ACTIVE credentials can be primary"))
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.path").value("/api/v1/profile/certificates/" + UUID_501 + "/primary"));
  }

    @Test
    void addCertificate_withoutToken_returns401UnifiedPayload() throws Exception {
        mockMvc
                .perform(
                        post("/api/v1/profile/certificates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                            "certificatePem": "-----BEGIN CERTIFICATE-----MIIB...-----END CERTIFICATE-----",
                                            "providerCode": "FNMT",
                                            "assuranceLevel": "high"
                                        }
                                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("API-401-001"))
                .andExpect(jsonPath("$.messageKey").value("error.common.unauthorized"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.path").value("/api/v1/profile/certificates"));
    }
}
