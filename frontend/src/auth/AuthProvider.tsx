import { useCallback, useEffect, useState, type ReactNode } from 'react';
import * as authApi from '../api/auth';
import * as usersApi from '../api/users';
import { clearTokens, getAccessToken, setTokens } from './tokenStorage';
import { AuthContext, type AuthContextType } from './AuthContext';
import type { AuthUser, RegisterRequest } from '../types/auth';
import type { UpdateUserRequest } from '../types/user';

interface AuthProviderProps {
  children: ReactNode;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    return response?.data?.message || fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  return fallback;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setError(null);
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
      setError(getErrorMessage(refreshError, 'Unable to restore session'));
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

    // Defer updating loading state to avoid synchronous setState during render
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
      setError(getErrorMessage(loginError, 'Login failed'));
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
      setError(getErrorMessage(registerError, 'Registration failed'));
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
      const refreshToken = localStorage.getItem('refreshToken');
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

  const updateProfile = useCallback(async (data: UpdateUserRequest) => {
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
      setError(getErrorMessage(profileError, 'Profile update failed'));
      throw profileError;
    } finally {
      setLoading(false);
    }
  }, [user]);

  const changePassword = useCallback(async (currentPassword: string, newPassword: string) => {
    try {
      setLoading(true);
      await authApi.changePassword({ currentPassword, newPassword });
    } catch (passwordError) {
      setError(getErrorMessage(passwordError, 'Password change failed'));
      throw passwordError;
    } finally {
      setLoading(false);
    }
  }, []);

  const hasRole = useCallback((role: string) => user?.roles?.includes(role) ?? false, [user]);
  const hasPermission = useCallback((permission: string) => user?.permissions?.includes(permission) ?? false, [user]);
  const hasAnyRole = useCallback((roles: string[]) => roles.some((role) => user?.roles?.includes(role ?? '')), [user]);
  const hasAnyPermission = useCallback((permissions: string[]) => permissions.some((permission) => user?.permissions?.includes(permission ?? '')), [user]);

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
    hasRole,
    hasPermission,
    hasAnyRole,
    hasAnyPermission,
    updateProfile,
    changePassword,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
