INSERT INTO "user" (
    username,
    full_name,
    phone_number,
    email,
    user_type,
    access_level,
    address_line,
    postal_code,
    city,
    country,
    default_language
)
VALUES
    (
        'superadmin',
        'Development Superadmin',
        '+420000000001',
        'superadmin@chinvat.local',
        'LIBRARY',
        'SUPERADMIN',
        'Developer Street 1',
        '00000',
        'Prague',
        'CZ',
        'en'
    ),
    (
        'admin',
        'Development Admin',
        '+420000000002',
        'admin@chinvat.local',
        'LIBRARY',
        'ADMIN',
        'Developer Street 2',
        '00000',
        'Prague',
        'CZ',
        'en'
    ),
    (
        'gold_user',
        'Development Gold User',
        '+420000000003',
        'gold@chinvat.local',
        'INDIVIDUAL',
        'GOLD',
        'Developer Street 3',
        '00000',
        'Prague',
        'CZ',
        'en'
    ),
    (
        'premium_user',
        'Development Premium User',
        '+420000000004',
        'premium@chinvat.local',
        'INDIVIDUAL',
        'PREMIUM',
        'Developer Street 4',
        '00000',
        'Prague',
        'CZ',
        'en'
    ),
    (
        'normal_user',
        'Development Normal User',
        '+420000000005',
        'normal@chinvat.local',
        'INDIVIDUAL',
        'NORMAL',
        'Developer Street 5',
        '00000',
        'Prague',
        'CZ',
        'en'
    )
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_password (
    user_id,
    password_hash,
    password_algorithm,
    recovery_required
)
SELECT
    id,
    crypt('DevPassword1234!', gen_salt('bf')),
    'bcrypt',
    FALSE
FROM "user"
WHERE username IN ('superadmin', 'admin', 'gold_user', 'premium_user', 'normal_user')
ON CONFLICT (user_id) DO UPDATE
SET
    password_hash = EXCLUDED.password_hash,
    password_algorithm = EXCLUDED.password_algorithm,
    password_changed_at = CURRENT_TIMESTAMP,
    recovery_required = EXCLUDED.recovery_required;

INSERT INTO rbac_role (role_name, permissions_csv)
VALUES
    ('USER', 'PROFILE:READ'),
    ('ADMIN', 'PROFILE:READ,PROFILE:WRITE,USERS:MANAGE'),
    ('SUPERADMIN', 'PROFILE:READ,PROFILE:WRITE,USERS:MANAGE,RBAC:MANAGE,AUTH:MANAGE')
ON CONFLICT (role_name) DO UPDATE
SET permissions_csv = EXCLUDED.permissions_csv;

INSERT INTO rbac_permission (permission_code, description)
VALUES
        ('PROFILE:READ', 'Read profile details'),
        ('PROFILE:WRITE', 'Update profile details'),
        ('USERS:MANAGE', 'Manage users'),
        ('RBAC:MANAGE', 'Manage RBAC model'),
        ('AUTH:MANAGE', 'Manage authentication flows')
ON CONFLICT (permission_code) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM rbac_role r
JOIN rbac_permission p
    ON p.permission_code = ANY (
            CASE r.role_name
                WHEN 'USER' THEN ARRAY['PROFILE:READ']
                WHEN 'ADMIN' THEN ARRAY['PROFILE:READ', 'PROFILE:WRITE', 'USERS:MANAGE']
                WHEN 'SUPERADMIN' THEN ARRAY['PROFILE:READ', 'PROFILE:WRITE', 'USERS:MANAGE', 'RBAC:MANAGE', 'AUTH:MANAGE']
                ELSE ARRAY[]::VARCHAR[]
            END
    )
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO user_role (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'seed:dev'
FROM "user" u
JOIN rbac_role r
    ON r.role_name =
            CASE
                WHEN u.username = 'superadmin' THEN 'SUPERADMIN'
                WHEN u.username = 'admin' THEN 'ADMIN'
                ELSE 'USER'
            END
WHERE u.username IN ('superadmin', 'admin', 'gold_user', 'premium_user', 'normal_user')
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO auth_audit_event (event_type, user_id, details)
SELECT
    'DEV_SEED_COMPLETED',
    id,
    jsonb_build_object('seed', '001_seed.sql')
FROM "user"
WHERE username = 'superadmin'
  AND NOT EXISTS (
      SELECT 1
      FROM auth_audit_event
      WHERE event_type = 'DEV_SEED_COMPLETED'
  );

