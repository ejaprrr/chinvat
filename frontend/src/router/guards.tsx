<<<<<<< HEAD
import { Navigate, Outlet } from 'react-router';
import type { ReactNode } from 'react';
import { useAuth } from '../contexts/auth';
import { appRoutes } from './routes';
=======
import { Navigate, Outlet } from "react-router";
import type { ReactNode } from "react";
import { useAuth } from "../contexts/auth";
import { appRoutes } from "./routes";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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
