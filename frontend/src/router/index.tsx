import { createBrowserRouter, Navigate } from 'react-router';
import AuthLayout from '../layouts/AuthLayout';
import LoginPage from '../pages/LoginPage';
import ProfilePage from '../pages/ProfilePage';
import RegisterPage from '../pages/RegisterPage';
import ResetPasswordPage from '../pages/ResetPasswordPage';
import { ProtectedRoute, PublicRoute } from './guards.tsx';
import { appRoutes } from './routes.ts';

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
          path: 'register',
          element: (
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          ),
        },
        {
          path: 'reset-password',
          element: <ResetPasswordPage />,
        },
        {
          path: '*',
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
