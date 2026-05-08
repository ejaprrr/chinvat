import type { ReactNode } from 'react';
import { Navigate, Outlet } from 'react-router';
import { useAuth } from './useAuth';

type ProtectedRouteProps = {
  children?: ReactNode;
};

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { authenticated, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!authenticated) {
    return <Navigate to="/" replace />;
  }

  return children ?? <Outlet />;
}
