import { createContext } from "react";
import type { AuthUser, RegisterRequest } from "../types/auth";
import type { UpdateUserRequest } from "../types/user";

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
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  updateProfile: (data: UpdateUserRequest) => Promise<void>;
  changePassword: (
    currentPassword: string,
    newPassword: string,
  ) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
