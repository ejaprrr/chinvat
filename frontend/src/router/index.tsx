/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate, Outlet } from "react-router";
import AuthLayout from "../layouts/AuthLayout";
import LoginPage from "../pages/LoginPage";
import ProfilePage from "../pages/ProfilePage";
import RegisterPage from "../pages/RegisterPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import { useAuth } from "../contexts/auth";

import type { ReactNode } from "react";
export const appRoutes = {
  login: "/",
  profile: "/profile",
  register: "/register",
  resetPassword: "/reset-password",
} as const;

function ProtectedRoute({ children }: { children?: ReactNode }) {
  const { authenticated, loading } = useAuth();

  if (loading) return null;
  if (!authenticated) return <Navigate to={appRoutes.login} replace />;
  return children ?? <Outlet />;
}

function PublicRoute({ children }: { children: ReactNode }) {
  const { authenticated, loading } = useAuth();

  if (loading) return null;
  if (authenticated) return <Navigate to={appRoutes.profile} replace />;
  return <>{children}</>;
}

export const router = createBrowserRouter(
  [
    {
      path: appRoutes.profile,
      element: (
        <ProtectedRoute>
          <ProfilePage />
        </ProtectedRoute>
      ),
    },
    {
      element: <AuthLayout />,
      children: [
        {
          index: true,
          element: (
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          ),
        },
        {
          path: "register",
          element: (
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          ),
        },
        {
          path: "reset-password",
          element: <ResetPasswordPage />,
        },
        {
          path: "*",
          element: <Navigate to={appRoutes.login} replace />,
        },
      ],
    },
  ],
  {
    basename: import.meta.env.BASE_URL,
  },
);

export default router;
