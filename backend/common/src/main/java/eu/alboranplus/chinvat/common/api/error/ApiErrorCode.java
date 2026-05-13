package eu.alboranplus.chinvat.common.api.error;

public enum ApiErrorCode {
  AUTH_INVALID_AUTHENTICATION(
      "AUTH-401-001", "error.auth.invalid-authentication", "Invalid authentication credentials."),
  AUTH_RESOURCE_NOT_FOUND(
      "AUTH-404-001", "error.auth.resource-not-found", "Requested authentication resource was not found."),

  USERS_ALREADY_EXISTS(
      "USR-409-001", "error.users.already-exists", "User already exists."),
  USERS_NOT_FOUND("USR-404-001", "error.users.not-found", "User was not found."),

  RBAC_ROLE_NOT_FOUND("RBAC-404-001", "error.rbac.role-not-found", "Role was not found."),
  RBAC_PERMISSION_NOT_FOUND(
      "RBAC-404-002", "error.rbac.permission-not-found", "Permission was not found."),
  RBAC_USER_NOT_FOUND("RBAC-404-003", "error.rbac.user-not-found", "RBAC user was not found."),
  RBAC_PERMISSION_ALREADY_EXISTS(
      "RBAC-409-001", "error.rbac.permission-already-exists", "Permission already exists."),

  EIDAS_PROVIDER_NOT_FOUND(
      "EIDAS-404-001", "error.eidas.provider-not-found", "eIDAS provider was not found."),
  EIDAS_INVALID_STATE(
      "EIDAS-401-001", "error.eidas.invalid-state", "eIDAS session state is invalid or expired."),
  EIDAS_BROKER_UNAVAILABLE(
      "EIDAS-503-001", "error.eidas.broker-unavailable", "eIDAS broker is currently unavailable."),
  EIDAS_EXTERNAL_IDENTITY_NOT_FOUND(
      "EIDAS-404-002", "error.eidas.external-identity-not-found", "Pending eIDAS external identity was not found."),
  EIDAS_PROFILE_COMPLETION_REQUIRED(
      "EIDAS-409-001", "error.eidas.profile-completion-required", "Profile completion is required."),

  TRUST_VALIDATION_FAILED(
      "TRUST-400-001", "error.trust.validation-failed", "Certificate validation failed."),
  TRUST_PROVIDER_SYNC_FAILED(
      "TRUST-503-001", "error.trust.provider-sync-failed", "Trust provider synchronization failed."),
  TRUST_CREDENTIAL_NOT_FOUND(
      "TRUST-404-001", "error.trust.credential-not-found", "Certificate credential was not found."),

  PROFILE_VALIDATION_FAILED(
      "PRF-400-001", "error.profile.validation-failed", "Profile validation failed."),
  PROFILE_INVALID_STATE(
      "PRF-400-002", "error.profile.invalid-state", "Profile request is not valid in the current state."),
  PROFILE_CREDENTIAL_NOT_FOUND(
      "PRF-404-001", "error.profile.credential-not-found", "Profile certificate credential was not found."),

  COMMON_VALIDATION_FAILED(
      "API-400-001", "error.common.validation-failed", "Validation failed."),
  COMMON_RESOURCE_NOT_FOUND(
      "API-404-001", "error.common.resource-not-found", "Requested resource was not found."),
  COMMON_UNAUTHORIZED("API-401-001", "error.common.unauthorized", "Authentication is required."),
  COMMON_FORBIDDEN("API-403-001", "error.common.forbidden", "Access is forbidden."),
  COMMON_RATE_LIMIT_EXCEEDED(
      "API-429-001", "error.common.rate-limit-exceeded", "Rate limit exceeded."),
  COMMON_INTERNAL_ERROR(
      "API-500-001", "error.common.internal-error", "Unexpected internal error.");

  private final String code;
  private final String messageKey;
  private final String defaultMessage;

  ApiErrorCode(String code, String messageKey, String defaultMessage) {
    this.code = code;
    this.messageKey = messageKey;
    this.defaultMessage = defaultMessage;
  }

  public String code() {
    return code;
  }

  public String messageKey() {
    return messageKey;
  }

  public String defaultMessage() {
    return defaultMessage;
  }
}
