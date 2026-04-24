import { createBrowserRouter, Navigate } from "react-router";
import AuthLayout from "../layouts/AuthLayout";
import LoginPage from "../pages/LoginPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import { appRoutes, authRouteSegments } from "./paths";

export const router = createBrowserRouter(
  [
    {
      element: <AuthLayout />,
      children: [
        {
          index: true,
          element: <LoginPage />,
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
