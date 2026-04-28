export const appRoutes = {
  login: "/",
  register: "/register",
  resetPassword: "/reset-password",
} as const;

export const authRouteSegments = {
  register: "register",
  resetPassword: "reset-password",
} as const;
