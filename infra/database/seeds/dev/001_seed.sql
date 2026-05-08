-- Chinvat Dev Seed Data
-- Complete permission and role structure for development testing

-- Seed all base permissions for the system
INSERT INTO "rbac_permission" ("permission_code", "description")
VALUES
    -- Authentication and account management
    ('AUTH:LOGIN', 'User login'),
    ('AUTH:LOGOUT', 'User logout'),
    ('AUTH:REFRESH', 'Refresh authentication token'),
    ('AUTH:CHANGE_PASSWORD', 'Change own password'),
    ('AUTH:RESET_PASSWORD', 'Request password reset'),
    ('AUTH:MANAGE', 'Manage authentication settings'),
    
    -- User profile management
    ('PROFILE:READ', 'View own profile'),
    ('PROFILE:EDIT', 'Edit own profile'),
    ('PROFILE:VIEW_DETAILS', 'View user profile details'),
    
    -- User management
    ('USERS:LIST', 'List all users'),
    ('USERS:VIEW', 'View user details'),
    ('USERS:CREATE', 'Create new user'),
    ('USERS:EDIT', 'Edit user details'),
    ('USERS:DELETE', 'Delete user'),
    ('USERS:MANAGE', 'Manage users'),
    ('USERS:ASSIGN_ROLES', 'Assign roles to users'),
    
    -- RBAC management
    ('RBAC:VIEW_ROLES', 'View roles'),
    ('RBAC:CREATE_ROLE', 'Create roles'),
    ('RBAC:EDIT_ROLE', 'Edit roles'),
    ('RBAC:DELETE_ROLE', 'Delete roles'),
    ('RBAC:VIEW_PERMISSIONS', 'View permissions'),
    ('RBAC:CREATE_PERMISSION', 'Create permissions'),
    ('RBAC:EDIT_PERMISSION', 'Edit permissions'),
    ('RBAC:DELETE_PERMISSION', 'Delete permissions'),
    ('RBAC:ASSIGN_PERMISSIONS', 'Assign permissions to roles'),
    ('RBAC:MANAGE', 'Manage RBAC model'),
    
    -- Audit and monitoring
    ('AUDIT:VIEW', 'View audit logs'),
    ('AUDIT:MANAGE', 'Manage audit settings'),
    
    -- System administration
    ('SYSTEM:ADMIN', 'Full system administration'),
    ('SYSTEM:SETTINGS', 'Manage system settings')
ON CONFLICT ("permission_code") DO NOTHING;

-- Create roles with proper hierarchy
INSERT INTO "rbac_role" ("role_name")
VALUES
    ('SUPERADMIN'),
    ('ADMIN'),
    ('MANAGER'),
    ('USER')
ON CONFLICT ("role_name") DO NOTHING;

-- Assign permissions to SUPERADMIN role (full access)
INSERT INTO "rbac_role_permission" ("role_id", "permission_id")
SELECT r."id", p."id"
FROM "rbac_role" r
CROSS JOIN "rbac_permission" p
WHERE r."role_name" = 'SUPERADMIN'
ON CONFLICT ("role_id", "permission_id") DO NOTHING;

-- Assign permissions to ADMIN role (management except system settings)
INSERT INTO "rbac_role_permission" ("role_id", "permission_id")
SELECT r."id", p."id"
FROM "rbac_role" r
JOIN "rbac_permission" p ON p."permission_code" IN (
    'AUTH:LOGIN', 'AUTH:LOGOUT', 'AUTH:REFRESH', 'AUTH:CHANGE_PASSWORD',
    'PROFILE:READ', 'PROFILE:EDIT', 'PROFILE:VIEW_DETAILS',
    'USERS:LIST', 'USERS:VIEW', 'USERS:CREATE', 'USERS:EDIT', 'USERS:DELETE', 'USERS:MANAGE', 'USERS:ASSIGN_ROLES',
    'RBAC:VIEW_ROLES', 'RBAC:VIEW_PERMISSIONS', 'RBAC:CREATE_ROLE', 'RBAC:EDIT_ROLE', 
    'RBAC:CREATE_PERMISSION', 'RBAC:EDIT_PERMISSION', 'RBAC:ASSIGN_PERMISSIONS', 'RBAC:MANAGE',
    'AUDIT:VIEW'
)
WHERE r."role_name" = 'ADMIN'
ON CONFLICT ("role_id", "permission_id") DO NOTHING;

-- Assign permissions to MANAGER role (user management)
INSERT INTO "rbac_role_permission" ("role_id", "permission_id")
SELECT r."id", p."id"
FROM "rbac_role" r
JOIN "rbac_permission" p ON p."permission_code" IN (
    'AUTH:LOGIN', 'AUTH:LOGOUT', 'AUTH:REFRESH', 'AUTH:CHANGE_PASSWORD',
    'PROFILE:READ', 'PROFILE:EDIT', 'PROFILE:VIEW_DETAILS',
    'USERS:LIST', 'USERS:VIEW', 'USERS:ASSIGN_ROLES'
)
WHERE r."role_name" = 'MANAGER'
ON CONFLICT ("role_id", "permission_id") DO NOTHING;

-- Assign permissions to USER role (basic access)
INSERT INTO "rbac_role_permission" ("role_id", "permission_id")
SELECT r."id", p."id"
FROM "rbac_role" r
JOIN "rbac_permission" p ON p."permission_code" IN (
    'AUTH:LOGIN', 'AUTH:LOGOUT', 'AUTH:REFRESH', 'AUTH:CHANGE_PASSWORD',
    'PROFILE:READ', 'PROFILE:EDIT'
)
WHERE r."role_name" = 'USER'
ON CONFLICT ("role_id", "permission_id") DO NOTHING;

-- Create development users
INSERT INTO "user" (
    "username", "full_name", "phone_number", "email", "user_type", "access_level",
    "address_line", "postal_code", "city", "country", "default_language"
)
VALUES
    ('superadmin', 'Dev Superadmin', '+420111111111', 'superadmin@chinvat.dev', 'LIBRARY', 'SUPERADMIN',
     'Dev Street 1', '11000', 'Prague', 'CZ', 'en'),
    ('admin', 'Dev Admin', '+420222222222', 'admin@chinvat.dev', 'LIBRARY', 'ADMIN',
     'Dev Street 2', '12000', 'Prague', 'CZ', 'en'),
    ('manager', 'Dev Manager', '+420333333333', 'manager@chinvat.dev', 'INDIVIDUAL', 'GOLD',
     'Dev Street 3', '13000', 'Prague', 'CZ', 'en'),
    ('john_doe', 'John Doe', '+420444444444', 'john@chinvat.dev', 'INDIVIDUAL', 'PREMIUM',
     'Dev Street 4', '14000', 'Prague', 'CZ', 'en'),
    ('jane_smith', 'Jane Smith', '+420555555555', 'jane@chinvat.dev', 'INDIVIDUAL', 'NORMAL',
     'Dev Street 5', '15000', 'Prague', 'CZ', 'en')
ON CONFLICT ("username") DO NOTHING;

-- Set passwords for all development users (password: DevPassword1234!)
INSERT INTO "user_password" ("user_id", "password_hash", "password_algorithm", "recovery_required")
SELECT 
    u."id",
    crypt('DevPassword1234!', gen_salt('bf')),
    'bcrypt',
    FALSE
FROM "user" u
WHERE u."username" IN ('superadmin', 'admin', 'manager', 'john_doe', 'jane_smith')
ON CONFLICT ("user_id") DO UPDATE
SET 
    "password_hash" = EXCLUDED."password_hash",
    "password_algorithm" = EXCLUDED."password_algorithm",
    "password_changed_at" = CURRENT_TIMESTAMP;

-- Assign roles to users
INSERT INTO "user_role" ("user_id", "role_id", "assigned_by")
SELECT 
    u."id",
    r."id",
    'seed:dev'
FROM "user" u
JOIN "rbac_role" r ON r."role_name" = CASE
    WHEN u."username" = 'superadmin' THEN 'SUPERADMIN'
    WHEN u."username" = 'admin' THEN 'ADMIN'
    WHEN u."username" = 'manager' THEN 'MANAGER'
    ELSE 'USER'
END
WHERE u."username" IN ('superadmin', 'admin', 'manager', 'john_doe', 'jane_smith')
ON CONFLICT ("user_id", "role_id") DO NOTHING;

-- Log seed completion
INSERT INTO "auth_audit_event" ("event_type", "user_id", "details")
SELECT 
    'DEV_SEED_COMPLETED',
    u."id",
    jsonb_build_object('seed', '001_seed.sql')
FROM "user" u
WHERE u."username" = 'superadmin'
  AND NOT EXISTS (
      SELECT 1
      FROM "auth_audit_event"
      WHERE "event_type" = 'DEV_SEED_COMPLETED'
  );

