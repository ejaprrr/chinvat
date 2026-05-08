/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate } from "react-router";
import type { ReactNode } from "react";
import AuthLayout from "../layouts/AuthLayout";
import LoginPage from "../pages/LoginPage.tsx";
import ProfilePage from "../pages/ProfilePage.tsx";
import RegisterPage from "../pages/RegisterPage.tsx";
import ResetPasswordPage from "../pages/ResetPasswordPage.tsx";
import { appRoutes, authRouteSegments } from "./paths";
import { ProtectedRoute } from "../auth/ProtectedRoute";
import { useAuth } from "../auth/useAuth";

function PublicRoute({ children }: { children: ReactNode }) {
  const { authenticated, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (authenticated) {
    return <Navigate to={appRoutes.profile} replace />;
  }

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
          path: authRouteSegments.register,
          element: (
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          ),
        },
        {
          path: authRouteSegments.resetPassword,
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
