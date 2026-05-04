import { createBrowserRouter, Navigate } from "react-router";
import AuthLayout from "../layouts/AuthLayout";
import LoginPage from "../pages/LoginPage";
import ProfilePage from "../pages/ProfilePage";
import RegisterPage from "../pages/RegisterPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import { appRoutes, authRouteSegments } from "./paths";

export const router = createBrowserRouter(
  [
    {
      path: authRouteSegments.profile,
      element: <ProfilePage />,
    },
    {
      element: <AuthLayout />,
      children: [
        {
          index: true,
          element: <LoginPage />,
        },
        {
          path: authRouteSegments.register,
          element: <RegisterPage />,
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
