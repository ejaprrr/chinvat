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
                77L,
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
                501L,
                77L,
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
        .willReturn(new EidasProfileCompletionView("EIDAS_EU", "subject-1", 77L, "ACTIVE", now, now));

    given(trustFacade.setPrimaryCertificateCredential(77L, 501L, "maria@example.com"))
        .willReturn(
            new CertificateCredentialView(
                501L,
                77L,
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
        .andExpect(jsonPath("$.userId").value(77))
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
                    77L,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(Optional.of(new UserSecurityView(77L, "maria@example.com", "Maria", Set.of("USER"), true)));

    given(trustFacade.listCertificateCredentials(77L))
        .willReturn(
            List.of(
                new CertificateCredentialView(
                    501L,
                    77L,
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
                777L,
                77L,
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

    willDoNothing().given(trustFacade).revokeCertificateCredential(501L, "maria@example.com", "ROTATED");

    given(trustFacade.setPrimaryCertificateCredential(77L, 501L, "maria@example.com"))
        .willReturn(
            new CertificateCredentialView(
                501L,
                77L,
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
        .andExpect(jsonPath("$[0].id").value(501))
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
        .andExpect(jsonPath("$.id").value(777));

    mockMvc
        .perform(
            delete("/api/v1/profile/certificates/501")
                .queryParam("reason", "ROTATED")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/profile/certificates/501/primary")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(501))
        .andExpect(jsonPath("$.primary").value(true));
  }

  @Test
  void setPrimaryCertificate_whenCredentialMissing_returns404() throws Exception {
    given(authFacade.validateAccessToken("token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    77L,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(Optional.of(new UserSecurityView(77L, "maria@example.com", "Maria", Set.of("USER"), true)));
    given(trustFacade.setPrimaryCertificateCredential(77L, 999L, "maria@example.com"))
        .willThrow(new CertificateCredentialNotFoundException("Certificate credential not found: 999"));

    mockMvc
        .perform(post("/api/v1/profile/certificates/999/primary").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Certificate credential not found: 999"));
  }

  @Test
  void setPrimaryCertificate_whenCredentialNotActive_returns400() throws Exception {
    given(authFacade.validateAccessToken("token"))
        .willReturn(
            Optional.of(
                new TokenPrincipal(
                    77L,
                    "maria@example.com",
                    Set.of("USER"),
                    Set.of("PROFILE:READ", "PROFILE:WRITE"))));
    given(usersFacade.findSecurityViewByEmail("maria@example.com"))
        .willReturn(Optional.of(new UserSecurityView(77L, "maria@example.com", "Maria", Set.of("USER"), true)));
    given(trustFacade.setPrimaryCertificateCredential(77L, 501L, "maria@example.com"))
        .willThrow(new IllegalStateException("Only ACTIVE credentials can be primary"));

    mockMvc
        .perform(post("/api/v1/profile/certificates/501/primary").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Only ACTIVE credentials can be primary"));
  }
}
