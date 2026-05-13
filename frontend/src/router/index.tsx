<<<<<<< HEAD
import { createBrowserRouter, Navigate } from 'react-router';
import AuthLayout from '../layouts/AuthLayout';
import LoginPage from '../pages/LoginPage';
import ProfilePage from '../pages/ProfilePage';
import RegisterPage from '../pages/RegisterPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import { ProtectedRoute, PublicRoute } from './guards.tsx';
import { appRoutes } from './routes.ts';
=======
import { createBrowserRouter, Navigate } from "react-router";
import AuthLayout from "../layouts/AuthLayout";
import LoginPage from "../pages/LoginPage";
import ProfilePage from "../pages/ProfilePage";
import RegisterPage from "../pages/RegisterPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import { ProtectedRoute, PublicRoute } from "./guards.tsx";
import { appRoutes } from "./routes.ts";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0

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
<<<<<<< HEAD
          path: 'register',
=======
          path: "register",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
          element: (
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          ),
        },
        {
<<<<<<< HEAD
          path: 'reset-password',
          element: <ResetPasswordPage />,
        },
        {
          path: '*',
=======
          path: "reset-password",
          element: <ResetPasswordPage />,
        },
        {
          path: "*",
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
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
