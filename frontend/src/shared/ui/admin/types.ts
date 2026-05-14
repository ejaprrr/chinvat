export type BindCredentialFormState = {
  userId: string;
  providerCode: string;
  registrationSource: string;
  assuranceLevel: string;
  certificatePem: string;
};

export type PermissionFormState = {
  code: string;
  description: string;
};

export type CreateUserFormState = {
  username: string;
  fullName: string;
  phoneNumber?: string;
  email: string;
  password: string;
  userType: 'INDIVIDUAL' | 'LIBRARY';
  addressLine?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  defaultLanguage: string;
  accessLevel: 'SUPERADMIN' | 'ADMIN' | 'GOLD' | 'PREMIUM' | 'NORMAL';
};
