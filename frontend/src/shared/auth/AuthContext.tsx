/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import * as authApi from '@/features/auth/api';
import * as usersApi from '@/features/profile/api';
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from '@/shared/auth/tokenStorage';
import { getErrorDisplay } from '@/shared/api/errors';
import type { AuthUser, RegisterRequest } from '@/shared/types/auth';
import type { UpdateUserRequest } from '@/shared/types/user';

export interface AuthContextType {
  user: AuthUser | null;
  loading: boolean;
  authenticated: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  clearError: () => void;
  reportError: (message: string) => void;
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  updateProfile: (data: UpdateUserRequest) => Promise<void>;
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

function getErrorMessage(error: unknown, fallbackCode: string) {
  return getErrorDisplay(error, {
    fallbackCode,
    fallbackMessage: fallbackCode,
  }).message;
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const reportError = useCallback((message: string) => {
    setError(message);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const currentUser = await authApi.getCurrentUser();
      setUser(currentUser);
      setAuthenticated(true);
    } catch (refreshError) {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        clearTokens();
        setUser(null);
        setAuthenticated(false);
        setError(getErrorMessage(refreshError, 'AUTH_SESSION_RESTORE_FAILED'));
        return;
      }

      try {
        const response = await authApi.refreshTokens({ refreshToken });
        setTokens(response.tokens.accessToken, response.tokens.refreshToken);
        setUser(response.user);
        setAuthenticated(true);
      } catch (tokenRefreshError) {
        clearTokens();
        setUser(null);
        setAuthenticated(false);
        setError(getErrorMessage(tokenRefreshError, 'AUTH_SESSION_RESTORE_FAILED'));
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (getAccessToken()) {
      Promise.resolve().then(() => {
        void refreshUser();
      });
      return;
    }

    Promise.resolve().then(() => setLoading(false));
  }, [refreshUser]);

  const login = useCallback(async (email: string, password: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await authApi.login({ email, password });
      setTokens(response.tokens.accessToken, response.tokens.refreshToken);
      setUser(response.user);
      setAuthenticated(true);
    } catch (loginError) {
      clearTokens();
      setAuthenticated(false);
      setUser(null);
      setError(getErrorMessage(loginError, 'AUTH_LOGIN_FAILED'));
      throw loginError;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    try {
      setLoading(true);
      setError(null);
      const response = await authApi.register(data);
      setTokens(response.tokens.accessToken, response.tokens.refreshToken);
      setUser(response.user);
      setAuthenticated(true);
    } catch (registerError) {
      clearTokens();
      setAuthenticated(false);
      setUser(null);
      setError(getErrorMessage(registerError, 'AUTH_REGISTER_FAILED'));
      throw registerError;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      setLoading(true);
      const accessToken = getAccessToken();
      const refreshToken = getRefreshToken();

      if (accessToken && refreshToken) {
        try {
          await authApi.logout({ accessToken, refreshToken });
        } catch {
          // Clear local session even if server-side revocation fails.
        }
      }
    } finally {
      clearTokens();
      setUser(null);
      setAuthenticated(false);
      setError(null);
      setLoading(false);
    }
  }, []);

  const updateProfile = useCallback(
    async (data: UpdateUserRequest) => {
      try {
        setLoading(true);

        if (!user?.id) {
          throw new Error('User not authenticated');
        }

        const updated = await usersApi.updateUser(user.id, data);
        setUser((current) =>
          current
            ? {
                ...current,
                displayName: updated.fullName,
                email: updated.email,
              }
            : current,
        );
      } catch (profileError) {
        setError(getErrorMessage(profileError, 'PROFILE_UPDATE_FAILED'));
        throw profileError;
      } finally {
        setLoading(false);
      }
    },
    [user],
  );

  const changePassword = useCallback(async (currentPassword: string, newPassword: string) => {
    try {
      setLoading(true);
      await authApi.changePassword({ currentPassword, newPassword });
    } catch (passwordError) {
      setError(getErrorMessage(passwordError, 'AUTH_PASSWORD_CHANGE_FAILED'));
      throw passwordError;
    } finally {
      setLoading(false);
    }
  }, []);

  const hasRole = useCallback((role: string) => user?.roles?.includes(role) ?? false, [user]);

  const hasPermission = useCallback(
    (permission: string) => user?.permissions?.includes(permission) ?? false,
    [user],
  );

  const hasAnyRole = useCallback(
    (roles: string[]) => roles.some((role) => user?.roles?.includes(role)),
    [user],
  );

  const hasAnyPermission = useCallback(
    (permissions: string[]) =>
      permissions.some((permission) => user?.permissions?.includes(permission)),
    [user],
  );

  const value: AuthContextType = {
    user,
    loading,
    authenticated,
    error,
    login,
    register,
    logout,
    refreshUser,
    clearError,
    reportError,
    hasRole,
    hasPermission,
    hasAnyRole,
    hasAnyPermission,
    updateProfile,
    changePassword,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);

  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return ctx;
}
