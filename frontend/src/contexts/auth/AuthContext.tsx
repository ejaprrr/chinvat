/* eslint-disable react-refresh/only-export-components */
<<<<<<< HEAD
import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import * as authApi from '../../lib/api/auth';
import * as usersApi from '../../lib/api/users';
import { getErrorDisplay } from '../../lib/http/errors';
import { clearTokens, getAccessToken, setTokens } from '../../lib/auth/tokenStorage';
import type { AuthUser, RegisterRequest } from '../../types/auth';
import type { UpdateUserRequest } from '../../types/user';
=======
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import * as authApi from "../../lib/api/auth";
import * as usersApi from "../../lib/api/users";
import { getErrorDisplay } from "../../lib/http/errors";
import {
  clearTokens,
  getAccessToken,
  setTokens,
} from "../../lib/auth/tokenStorage";
import type { AuthUser, RegisterRequest } from "../../types/auth";
import type { UpdateUserRequest } from "../../types/user";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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
<<<<<<< HEAD
  changePassword: (currentPassword: string, newPassword: string) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
=======
  changePassword: (
    currentPassword: string,
    newPassword: string,
  ) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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

<<<<<<< HEAD
  const clearError = useCallback(() => {
=======
  useEffect(() => {
    if (!error) return;
    try {
      // expose last error for debugging regardless of how it was set
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      if (typeof window !== "undefined") window.__lastAuthError = error;
    } catch {
      /* ignore */
    }
    // eslint-disable-next-line no-console
    console.error("AuthProvider.error changed:", error);
  }, [error]);

  const clearError = useCallback(() => {
    try {
      // Debug: log when error is cleared and stack trace
      // eslint-disable-next-line no-console
      console.error("clearError() called. Stack:", new Error().stack);
    } catch {
      /* ignore */
    }
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    setError(null);
  }, []);

  const reportError = useCallback((message: string) => {
<<<<<<< HEAD
=======
    try {
      // Helpful debug hook for runtime inspection
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      if (typeof window !== "undefined") window.__lastAuthError = message;
    } catch {
      /* ignore */
    }
    // also log so it's visible in devtools console when reproducing
    // eslint-disable-next-line no-console
    console.error("AuthProvider.reportError:", message);
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    setError(message);
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      setLoading(true);
      const currentUser = await authApi.getCurrentUser();
      setUser(currentUser);
      setAuthenticated(true);
      setError(null);
    } catch (refreshError) {
      setUser(null);
      setAuthenticated(false);
<<<<<<< HEAD
      setError(getErrorMessage(refreshError, 'AUTH_SESSION_RESTORE_FAILED'));
=======
      setError(getErrorMessage(refreshError, "AUTH_SESSION_RESTORE_FAILED"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      clearTokens();
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (getAccessToken()) {
      void (async () => {
        await refreshUser();
      })();
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
      setAuthenticated(false);
      setUser(null);
<<<<<<< HEAD
      setError(getErrorMessage(loginError, 'AUTH_LOGIN_FAILED'));
=======
      setError(getErrorMessage(loginError, "AUTH_LOGIN_FAILED"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      clearTokens();
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
      setAuthenticated(false);
      setUser(null);
<<<<<<< HEAD
      setError(getErrorMessage(registerError, 'AUTH_REGISTER_FAILED'));
=======
      setError(getErrorMessage(registerError, "AUTH_REGISTER_FAILED"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
      clearTokens();
      throw registerError;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      setLoading(true);
      const accessToken = getAccessToken();
<<<<<<< HEAD
      const refreshToken = localStorage.getItem('refreshToken');
=======
      const refreshToken = localStorage.getItem("refreshToken");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
          throw new Error('User not authenticated');
=======
          throw new Error("User not authenticated");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
        setError(getErrorMessage(profileError, 'PROFILE_UPDATE_FAILED'));
=======
        setError(getErrorMessage(profileError, "PROFILE_UPDATE_FAILED"));
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
        throw profileError;
      } finally {
        setLoading(false);
      }
    },
    [user],
  );

<<<<<<< HEAD
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
=======
  const changePassword = useCallback(
    async (currentPassword: string, newPassword: string) => {
      try {
        setLoading(true);
        await authApi.changePassword({ currentPassword, newPassword });
      } catch (passwordError) {
        setError(getErrorMessage(passwordError, "AUTH_PASSWORD_CHANGE_FAILED"));
        throw passwordError;
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  const hasRole = useCallback(
    (role: string) => user?.roles?.includes(role) ?? false,
    [user],
  );
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  const hasPermission = useCallback(
    (permission: string) => user?.permissions?.includes(permission) ?? false,
    [user],
  );
  const hasAnyRole = useCallback(
<<<<<<< HEAD
    (roles: string[]) => roles.some((role) => user?.roles?.includes(role ?? '')),
=======
    (roles: string[]) =>
      roles.some((role) => user?.roles?.includes(role ?? "")),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
    [user],
  );
  const hasAnyPermission = useCallback(
    (permissions: string[]) =>
<<<<<<< HEAD
      permissions.some((permission) => user?.permissions?.includes(permission ?? '')),
=======
      permissions.some((permission) =>
        user?.permissions?.includes(permission ?? ""),
      ),
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
<<<<<<< HEAD
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
=======
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  return ctx;
}
