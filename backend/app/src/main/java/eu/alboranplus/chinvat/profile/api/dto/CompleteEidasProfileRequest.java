package eu.alboranplus.chinvat.profile.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteEidasProfileRequest(
    @NotBlank String providerCode,
    @NotBlank String externalSubjectId,
    @NotBlank @Size(max = 100) String username,
    @NotBlank @Size(max = 255) String fullName,
    @Size(max = 40) String phoneNumber,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 12, max = 128) String password,
    @Size(max = 255) String addressLine,
    @Size(max = 20) String postalCode,
    @Size(max = 100) String city,
    @Size(max = 100) String country,
    @NotBlank @Size(max = 12) String defaultLanguage,
    @NotBlank String certificatePem,
    @Size(max = 80) String assuranceLevel,
    @Size(max = 80) String certificateProviderCode,
    @Size(max = 255) String identityReference,
    @Size(max = 80) String nationality) {}
