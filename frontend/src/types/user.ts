// User profile response from GET /api/v1/users/{id} and PUT /api/v1/users/{id}
export interface UserResponse {
  id: number;
  username: string;
  fullName: string;
  phoneNumber?: string;
  email: string;
  userType: string;
  accessLevel: string;
  addressLine?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
}

// Request payload for PUT /api/v1/users/{id}
export interface UpdateUserRequest {
  username: string;
  fullName: string;
  phoneNumber?: string;
<<<<<<< HEAD
  userType: 'INDIVIDUAL' | 'LIBRARY';
  accessLevel: 'SUPERADMIN' | 'ADMIN' | 'GOLD' | 'PREMIUM' | 'NORMAL';
=======
  userType: "INDIVIDUAL" | "LIBRARY";
  accessLevel: "SUPERADMIN" | "ADMIN" | "GOLD" | "PREMIUM" | "NORMAL";
>>>>>>> 573589ea5a4c169684a79711c7b60fc968c582e0
  addressLine?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
}

// RBAC Types
export interface UserRolesResponse {
  userId: number;
  roles: string[];
}

export interface RoleResponse {
  roleName: string;
  permissions: string[];
}

export interface PermissionResponse {
  code: string;
  description?: string;
}
