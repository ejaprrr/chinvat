// API Request Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  fullName: string;
  phoneNumber?: string;
  email: string;
  password: string;
<<<<<<< HEAD
  userType: 'INDIVIDUAL' | 'LIBRARY';
=======
  userType: "INDIVIDUAL" | "LIBRARY";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  addressLine?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  accessToken: string;
  refreshToken: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordResetConfirmRequest {
  email: string;
  resetCode: string;
  newPassword: string;
}

// API Response Types
export interface UserInfo {
  id: number;
  email: string;
  displayName: string;
  roles: string[];
  permissions: string[];
}

export interface TokenInfo {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
}

export interface AuthResponse {
  user: UserInfo;
  tokens: TokenInfo;
}

export interface AuthMeResponse {
  id: number;
  email: string;
  displayName: string;
  roles: string[];
  permissions: string[];
}

export interface PasswordResetRequestResponse {
  resetCode?: string | null;
}

export interface AuthSessionResponse {
  sessionId: string;
<<<<<<< HEAD
  tokenKind: 'ACCESS' | 'REFRESH';
=======
  tokenKind: "ACCESS" | "REFRESH";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  issuedAt: string;
  expiresAt: string;
  clientIp: string;
  userAgent: string;
}

// Internal Auth User Type (combining user + metadata)
export type AuthUser = AuthMeResponse;
