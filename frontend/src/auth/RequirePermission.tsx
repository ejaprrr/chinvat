import type { ReactNode } from 'react';
import { Navigate } from 'react-router';
import { useAuth } from './useAuth';

type RequirePermissionProps = {
  permission: string;
  children: ReactNode;
};

export function RequirePermission({ permission, children }: RequirePermissionProps) {
  const { loading, hasPermission } = useAuth();

  if (loading) {
    return null;
  }

  if (!hasPermission(permission)) {
    return <Navigate to="/profile" replace />;
  }

  return <>{children}</>;
}
