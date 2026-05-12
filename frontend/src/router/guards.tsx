import { Navigate, Outlet } from 'react-router';
import type { ReactNode } from 'react';
import { useAuth } from '../contexts/auth';
import { appRoutes } from './routes';

export function ProtectedRoute({ children }: { children?: ReactNode }) {
  const { authenticated, loading } = useAuth();

  if (loading) return null;
  if (!authenticated) return <Navigate to={appRoutes.login} replace />;
  return children ?? <Outlet />;
}

export function PublicRoute({ children }: { children: ReactNode }) {
  const { authenticated, loading } = useAuth();

  if (loading) return null;
  if (authenticated) return <Navigate to={appRoutes.profile} replace />;
  return <>{children}</>;
}
