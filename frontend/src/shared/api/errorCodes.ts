// Error code mapping following backend ApiErrorCode enum
// Maps error codes to i18n message keys for localization

export interface ErrorCodeDefinition {
  code: string;
  messageKey: string;
  defaultMessage: string;
}

export const ERROR_CODES: Record<string, ErrorCodeDefinition> = {
  // Authentication errors
  'AUTH-401-001': {
    code: 'AUTH-401-001',
    messageKey: 'error.auth.invalid-authentication',
    defaultMessage: 'Invalid authentication credentials.',
  },
  'AUTH-404-001': {
    code: 'AUTH-404-001',
    messageKey: 'error.auth.resource-not-found',
    defaultMessage: 'Requested authentication resource was not found.',
  },

  // User errors
  'USR-409-001': {
    code: 'USR-409-001',
    messageKey: 'error.users.already-exists',
    defaultMessage: 'User already exists.',
  },
  'USR-404-001': {
    code: 'USR-404-001',
    messageKey: 'error.users.not-found',
    defaultMessage: 'User was not found.',
  },

  // RBAC errors
  'RBAC-404-001': {
    code: 'RBAC-404-001',
    messageKey: 'error.rbac.role-not-found',
    defaultMessage: 'Role was not found.',
  },
  'RBAC-404-002': {
    code: 'RBAC-404-002',
    messageKey: 'error.rbac.permission-not-found',
    defaultMessage: 'Permission was not found.',
  },
  'RBAC-404-003': {
    code: 'RBAC-404-003',
    messageKey: 'error.rbac.user-not-found',
    defaultMessage: 'RBAC user was not found.',
  },
  'RBAC-409-001': {
    code: 'RBAC-409-001',
    messageKey: 'error.rbac.permission-already-exists',
    defaultMessage: 'Permission already exists.',
  },

  // eIDAS errors
  'EIDAS-404-001': {
    code: 'EIDAS-404-001',
    messageKey: 'error.eidas.provider-not-found',
    defaultMessage: 'eIDAS provider was not found.',
  },
  'EIDAS-401-001': {
    code: 'EIDAS-401-001',
    messageKey: 'error.eidas.invalid-state',
    defaultMessage: 'eIDAS session state is invalid or expired.',
  },
  'EIDAS-503-001': {
    code: 'EIDAS-503-001',
    messageKey: 'error.eidas.broker-unavailable',
    defaultMessage: 'eIDAS broker is currently unavailable.',
  },
  'EIDAS-404-002': {
    code: 'EIDAS-404-002',
    messageKey: 'error.eidas.external-identity-not-found',
    defaultMessage: 'Pending eIDAS external identity was not found.',
  },
  'EIDAS-409-001': {
    code: 'EIDAS-409-001',
    messageKey: 'error.eidas.profile-completion-required',
    defaultMessage: 'Profile completion is required.',
  },

  // Trust errors
  'TRUST-400-001': {
    code: 'TRUST-400-001',
    messageKey: 'error.trust.validation-failed',
    defaultMessage: 'Certificate validation failed.',
  },
  'TRUST-503-001': {
    code: 'TRUST-503-001',
    messageKey: 'error.trust.provider-sync-failed',
    defaultMessage: 'Trust provider synchronization failed.',
  },
  'TRUST-404-001': {
    code: 'TRUST-404-001',
    messageKey: 'error.trust.credential-not-found',
    defaultMessage: 'Certificate credential was not found.',
  },

  // Profile errors
  'PRF-400-001': {
    code: 'PRF-400-001',
    messageKey: 'error.profile.validation-failed',
    defaultMessage: 'Profile validation failed.',
  },
  'PRF-400-002': {
    code: 'PRF-400-002',
    messageKey: 'error.profile.invalid-state',
    defaultMessage: 'Profile request is not valid in the current state.',
  },
  'PRF-404-001': {
    code: 'PRF-404-001',
    messageKey: 'error.profile.credential-not-found',
    defaultMessage: 'Profile certificate credential was not found.',
  },

  // Common errors
  'API-400-001': {
    code: 'API-400-001',
    messageKey: 'error.common.validation-failed',
    defaultMessage: 'Validation failed.',
  },
  'API-404-001': {
    code: 'API-404-001',
    messageKey: 'error.common.resource-not-found',
    defaultMessage: 'Requested resource was not found.',
  },
  'API-401-001': {
    code: 'API-401-001',
    messageKey: 'error.common.unauthorized',
    defaultMessage: 'Authentication is required.',
  },
  'API-403-001': {
    code: 'API-403-001',
    messageKey: 'error.common.forbidden',
    defaultMessage: 'Access is forbidden.',
  },
  'API-429-001': {
    code: 'API-429-001',
    messageKey: 'error.common.rate-limit-exceeded',
    defaultMessage: 'Rate limit exceeded.',
  },
  'API-500-001': {
    code: 'API-500-001',
    messageKey: 'error.common.internal-error',
    defaultMessage: 'Unexpected internal error.',
  },
};

export function getErrorCodeDefinition(code: string | undefined): ErrorCodeDefinition | null {
  if (!code) return null;
  return ERROR_CODES[code] || null;
}

export function getErrorMessageKey(code: string | undefined): string | null {
  const definition = getErrorCodeDefinition(code);
  return definition?.messageKey || null;
}
