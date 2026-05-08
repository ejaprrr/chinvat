import type { ReactNode } from "react";
import { Navigate } from "react-router";
import { useAuth } from "./useAuth";

type RequireRoleProps = {
  role: string;
  children: ReactNode;
};

export function RequireRole({ role, children }: RequireRoleProps) {
  const { loading, hasRole } = useAuth();

  if (loading) {
    return null;
  }

  if (!hasRole(role)) {
    return <Navigate to="/profile" replace />;
  }

  return <>{children}</>;
}
