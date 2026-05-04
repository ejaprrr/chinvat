export const appRoutes = {
  login: "/",
  profile: "/profile",
  register: "/register",
  resetPassword: "/reset-password",
} as const;

export const authRouteSegments = {
  profile: "profile",
  register: "register",
  resetPassword: "reset-password",
} as const;
