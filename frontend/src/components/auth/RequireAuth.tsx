import type { ReactNode } from "react";
import { Navigate } from "react-router";
import { useAuth } from "../../auth/useAuth";

type RequireAuthProps = {
  children: ReactNode;
};

function RequireAuth({ children }: RequireAuthProps) {
  const { authenticated, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!authenticated) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

export default RequireAuth;
