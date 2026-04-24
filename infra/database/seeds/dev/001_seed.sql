INSERT INTO app_user (
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
VALUES (
    'admin',
    'Development Admin',
    '+420000000000',
    'admin@chinvat.local',
    'LIBRARY',
    'SUPERADMIN',
    'Developer Street 1',
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
    'CHANGE_ME_BEFORE_REAL_AUTH_IS_ENABLED',
    'placeholder',
    FALSE
FROM app_user
WHERE username = 'admin'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO auth_audit_event (event_type, user_id, details)
SELECT
    'DEV_SEED_COMPLETED',
    id,
    jsonb_build_object('seed', '001_seed.sql')
FROM app_user
WHERE username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM auth_audit_event
      WHERE event_type = 'DEV_SEED_COMPLETED'
  );

